/**
 * OpenSpotLight - Open Source IT Governance Platform
 *
 * Copyright (c) 2009, CARAVELATECH CONSULTORIA E TECNOLOGIA EM INFORMATICA LTDA
 * or third-party contributors as indicated by the @author tags or express
 * copyright attribution statements applied by the authors.  All third-party
 * contributions are distributed under license by CARAVELATECH CONSULTORIA E
 * TECNOLOGIA EM INFORMATICA LTDA.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 *
 ***********************************************************************
 * OpenSpotLight - Plataforma de Governança de TI de Código Aberto
 *
 * Direitos Autorais Reservados (c) 2009, CARAVELATECH CONSULTORIA E TECNOLOGIA
 * EM INFORMATICA LTDA ou como contribuidores terceiros indicados pela etiqueta
 * @author ou por expressa atribuição de direito autoral declarada e atribuída pelo autor.
 * Todas as contribuições de terceiros estão distribuídas sob licença da
 * CARAVELATECH CONSULTORIA E TECNOLOGIA EM INFORMATICA LTDA.
 *
 * Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo sob os
 * termos da Licença Pública Geral Menor do GNU conforme publicada pela Free Software
 * Foundation.
 *
 * Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA
 * GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU ADEQUAÇÃO A UMA
 * FINALIDADE ESPECÍFICA. Consulte a Licença Pública Geral Menor do GNU para mais detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral Menor do GNU junto com este
 * programa; se não, escreva para:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.openspotlight.bundle.scheduler;

import org.openspotlight.common.exception.SLRuntimeException;
import org.openspotlight.common.util.Assertions;
import org.openspotlight.common.util.Exceptions;
import org.openspotlight.common.util.SLCollections;
import org.openspotlight.bundle.context.ExecutionContext;
import org.openspotlight.bundle.context.ExecutionContextFactory;
import org.openspotlight.bundle.domain.GlobalSettings;
import org.openspotlight.bundle.domain.Repository;
import org.openspotlight.bundle.domain.Schedulable;
import org.openspotlight.bundle.domain.Schedulable.SchedulableCommand;
import org.openspotlight.bundle.domain.Schedulable.SchedulableCommandWithContextFactory;
import org.openspotlight.federation.util.RepositorySet;
import org.openspotlight.jcr.provider.JcrConnectionDescriptor;
import org.openspotlight.persist.util.SimpleNodeTypeVisitor;
import org.openspotlight.persist.util.SimpleNodeTypeVisitorSupport;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public enum DefaultScheduler implements SLScheduler {
    INSTANCE;
    private static class InternalData {
        public final String                  username;
        public final String                  password;
        public final JcrConnectionDescriptor descriptor;
        public final ExecutionContextFactory contextFactory;

        public InternalData(
                             final String username, final String password, final JcrConnectionDescriptor descriptor,
                             final ExecutionContextFactory contextFactory ) {
            super();
            this.username = username;
            this.password = password;
            this.descriptor = descriptor;
            this.contextFactory = contextFactory;
        }
    }

    public static class OslInternalImmediateCommand extends OslInternalSchedulerCommand {

        private final String identifier;

        @SuppressWarnings( "unchecked" )
        public OslInternalImmediateCommand(
                                            final Schedulable schedulable, final Class<? extends SchedulableCommand> commandType,
                                            final AtomicReference<InternalData> internalData,
                                            final AtomicReference<GlobalSettings> settings ) {
            super(schedulable, commandType, internalData, settings, IMMEDIATE);
            identifier = UUID.randomUUID().toString();

        }

        @Override
        public String getUniqueName() {
            return identifier;
        }

    }

    public static class OslInternalSchedulerCommand {

        private final String                              jobName;

        private final String                              cronInformation;

        private final AtomicReference<GlobalSettings>     settings;

        private final AtomicReference<InternalData>       internalData;

        @SuppressWarnings( "unchecked" )
        private final Class<? extends SchedulableCommand> commandType;

        private final Schedulable                         schedulable;

        private final Logger                              logger = LoggerFactory.getLogger(getClass());

        @SuppressWarnings( "unchecked" )
        public OslInternalSchedulerCommand(
                                            final Schedulable schedulable, final Class<? extends SchedulableCommand> commandType,
                                            final AtomicReference<InternalData> internalData,
                                            final AtomicReference<GlobalSettings> settings, final String cronInformation ) {
            this.schedulable = schedulable;
            this.settings = settings;
            this.internalData = internalData;
            this.commandType = commandType;
            this.cronInformation = cronInformation;
            jobName = schedulable.toUniqueJobString();
        }

        @SuppressWarnings( "unchecked" )
        public void execute() throws JobExecutionException {
            ExecutionContext context = null;
            try {
                final GlobalSettings settingsCopy = settings.get();
                final InternalData data = internalData.get();
                final SchedulableCommand<Schedulable> command = commandType.newInstance();
                final String repositoryName = command.getRepositoryNameBeforeExecution(schedulable);
                context = data.contextFactory.createExecutionContext(data.username, data.password, data.descriptor,
                                                                     schedulable.getRepositoryForSchedulable());

                if (command instanceof SchedulableCommandWithContextFactory) {
                    final SchedulableCommandWithContextFactory<Schedulable> commandWithFactory = (SchedulableCommandWithContextFactory<Schedulable>)command;
                    commandWithFactory.setContextFactoryBeforeExecution(settingsCopy, data.descriptor, data.username,
                                                                        data.password, repositoryName, data.contextFactory);
                }
                logger.info("about to execute " + command.getClass() + " with schedulable " + schedulable.toUniqueJobString());
                command.execute(settings.get(), context, schedulable);
                logger.info("executed successfully " + command.getClass() + " with schedulable "
                            + schedulable.toUniqueJobString());
            } catch (final Exception e) {
                Exceptions.logAndReturnNew(e, JobExecutionException.class);
            } finally {
                if (context != null) {
                    context.closeResources();
                }
            }
        }

        public String getCronInformation() {
            return cronInformation;
        }

        public String getJobName() {
            return jobName;
        }

        public String getUniqueName() {
            return jobName + ":" + cronInformation;
        }

    }

    public static class SchedulableVisitor implements SimpleNodeTypeVisitor<Schedulable> {

        private final List<Schedulable> beans = new LinkedList<Schedulable>();

        public List<Schedulable> getBeans() {
            return beans;
        }

        public void visitBean( final Schedulable bean ) {
            beans.add(bean);

        }

    }

    private final ConcurrentHashMap<String, OslInternalSchedulerCommand> oslCronCommands      = new ConcurrentHashMap<String, OslInternalSchedulerCommand>();

    private final ConcurrentHashMap<String, OslInternalSchedulerCommand> oslImmediateCommands = new ConcurrentHashMap<String, OslInternalSchedulerCommand>();

    private static String                                                DEFAULT_GROUP        = "osl jobs";

    private final AtomicReference<InternalData>                          internalData         = new AtomicReference<InternalData>();

    private final Scheduler                                              quartzScheduler;

    private final AtomicReference<GlobalSettings>                        settings             = new AtomicReference<GlobalSettings>();

    public static final String                                           IMMEDIATE            = "immediate";

    private DefaultScheduler() {
        try {
            // FIXME remove this
            System.setProperty("org.quartz.threadPool.threadCount", "1");
            quartzScheduler = new StdSchedulerFactory().getScheduler();
        } catch (final SchedulerException e) {
            throw Exceptions.logAndReturnNew(e, SLRuntimeException.class);
        }
    }

    public <T extends Schedulable> void fireSchedulable( final String username,
                                                         final String password,
                                                         final T... schedulables ) {
        final Set<String> ids = internalFireCommand(username, password, schedulables);
        final long sleep = settings.get().getDefaultSleepingIntervalInMilliseconds();
        while (isExecutingAnyOfImmediateCommands(ids)) {
            try {
                Thread.sleep(sleep);
            } catch (final InterruptedException e) {
            }
        }
    }

    public <T extends Schedulable> void fireSchedulableInBackground( final String username,
                                                                     final String password,
                                                                     final T... schedulables ) {
        internalFireCommand(username, password, schedulables);

    }

    OslInternalSchedulerCommand getCommandByName( final String name ) {
        OslInternalSchedulerCommand command = oslCronCommands.get(name);
        if (command == null) {
            command = oslImmediateCommands.get(name);
        }
        return command;
    }

    @SuppressWarnings( "unchecked" )
    private Map<String, OslInternalSchedulerCommand> groupJobsByCronInformation( final GlobalSettings settings,
                                                                                 final Iterable<Repository> repositories ) {
        @SuppressWarnings( "unused" )
        final Map<Class<? extends Schedulable>, Class<? extends SchedulableCommand>> commandMap = settings.getSchedulableCommandMap();

        final RepositorySet repositorySet = new RepositorySet();
        repositorySet.setRepositories(repositories);
        final SchedulableVisitor visitor = new SchedulableVisitor();
        SimpleNodeTypeVisitorSupport.acceptVisitorOn(Schedulable.class, repositorySet, visitor);
        final List<Schedulable> schedulableList = visitor.getBeans();
        final Map<String, OslInternalSchedulerCommand> newJobs = new HashMap<String, OslInternalSchedulerCommand>();
        for (final Schedulable s : schedulableList) {
            for (final String cronInformation : s.getCronInformation()) {

                final Class<? extends SchedulableCommand> commandType = settings.getSchedulableCommandMap().get(s.getClass());

                Assertions.checkNotNull("commandType:" + s.getClass(), commandType);

                final OslInternalSchedulerCommand job = new OslInternalSchedulerCommand(s, commandType, internalData,
                                                                                        this.settings, cronInformation);
                newJobs.put(job.getUniqueName(), job);
            }
        }
        return newJobs;
    }

    public void initializeSettings( final ExecutionContextFactory contextFactory,
                                    final String username,
                                    final String password,
                                    final JcrConnectionDescriptor descriptor ) {
        Assertions.checkNotEmpty("username", username);
        Assertions.checkNotEmpty("password", password);
        Assertions.checkNotNull("contextFactory", contextFactory);
        Assertions.checkNotNull("descriptor", descriptor);

        final InternalData newData = new InternalData(username, password, descriptor, contextFactory);
        internalData.set(newData);
    }

    @SuppressWarnings( "unchecked" )
    private <T extends Schedulable> Set<String> internalFireCommand( final String username,
                                                                     final String password,
                                                                     final T... schedulables ) {
        Assertions.checkNotNull("schedulables", schedulables);
        Assertions.checkNotNull("internalData", internalData.get());
        Assertions.checkNotNull("settings", settings.get());
        final Set<String> ids = new HashSet<String>();
        final GlobalSettings settingsReference = settings.get();
        for (final Schedulable schedulable : schedulables) {
            Assertions.checkNotNull("schedulable", schedulable);
            try {

                Class<? extends SchedulableCommand> commandType = null;
                Class<? extends Schedulable> lastClass = schedulable.getClass();
                while (commandType == null && lastClass != null && !Object.class.equals(lastClass)) {
                    commandType = settingsReference.getSchedulableCommandMap().get(lastClass);
                    if (commandType != null) {
                        break;
                    }
                    lastClass = (Class<? extends Schedulable>)lastClass.getSuperclass();
                }

                Assertions.checkNotNull("commandType:" + schedulable.getClass(), commandType);
                final InternalData copy = new InternalData(username, password, internalData.get().descriptor,
                                                           internalData.get().contextFactory);
                final AtomicReference<InternalData> copyRef = new AtomicReference<InternalData>(copy);
                final OslInternalImmediateCommand command = new OslInternalImmediateCommand(schedulable, commandType, copyRef,
                                                                                            settings);
                oslImmediateCommands.put(command.getUniqueName(), command);
                ids.add(command.getUniqueName());
                final Date runTime = TriggerUtils.getNextGivenSecondDate(new Date(), 1);
                final JobDetail job = new JobDetail(command.getUniqueName(), DEFAULT_GROUP, OslQuartzJob.class);
                final SimpleTrigger trigger = new SimpleTrigger(command.getUniqueName(), DEFAULT_GROUP, runTime);
                quartzScheduler.scheduleJob(job, trigger);
            } catch (final Exception e) {
                throw Exceptions.logAndReturnNew(e, SLRuntimeException.class);
            }

        }
        return ids;
    }

    private boolean isExecutingAnyOfImmediateCommands( final Set<String> ids ) {
        final Set<String> executingKeys = new HashSet<String>(oslImmediateCommands.keySet());
        for (final String id : ids) {
            if (executingKeys.contains(id)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void refreshJobs( final GlobalSettings settings,
                                          final Iterable<Repository> repositories ) {
        Assertions.checkNotNull("settings", settings);
        Assertions.checkNotNull("repositories", repositories);
        Assertions.checkNotNull("internalData", internalData.get());
        this.settings.set(settings);
        final Map<String, OslInternalSchedulerCommand> jobMap = groupJobsByCronInformation(settings, repositories);
        oslCronCommands.clear();
        oslCronCommands.putAll(jobMap);
        try {
            final Set<String> jobsToRemove = SLCollections.setOf(quartzScheduler.getJobNames(DEFAULT_GROUP));
            final Set<String> newJobNames = new HashSet<String>(jobMap.keySet());
            newJobNames.removeAll(jobsToRemove);

            jobsToRemove.removeAll(newJobNames);
            for (final String jobToRemove : jobsToRemove) {
                quartzScheduler.deleteJob(jobToRemove, DEFAULT_GROUP);
            }
            for (final String newJob : newJobNames) {
                final OslInternalSchedulerCommand command = jobMap.get(newJob);
                final JobDetail job = new JobDetail(command.getUniqueName(), DEFAULT_GROUP, OslQuartzJob.class);
                final CronTrigger trigger = new CronTrigger(command.getUniqueName(), DEFAULT_GROUP, command.getUniqueName(),
                                                            DEFAULT_GROUP, command.getCronInformation());
                quartzScheduler.scheduleJob(job, trigger);

            }
        } catch (final Exception e) {
            stopScheduler();
            throw Exceptions.logAndReturnNew(e, SLRuntimeException.class);
        }

    }

    public void removeIfImediate( final OslInternalSchedulerCommand command ) {
        Assertions.checkNotNull("command", command);
        if (IMMEDIATE.equals(command.getCronInformation())) {
            oslImmediateCommands.remove(command.getUniqueName());
        }

    }

    public void startScheduler() {
        Assertions.checkNotNull("internalData", internalData.get());
        try {
            quartzScheduler.start();
        } catch (final Exception e) {
            stopScheduler();
            throw Exceptions.logAndReturnNew(e, SLRuntimeException.class);
        }

    }

    public void stopScheduler() {
        try {
            quartzScheduler.shutdown();
        } catch (final Exception e) {
            throw Exceptions.logAndReturnNew(e, SLRuntimeException.class);
        }
    }

}