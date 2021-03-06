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
package org.openspotlight.graph.query.console.command.dynamic;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Test;
import org.openspotlight.graph.query.console.ConsoleState;
import org.openspotlight.graph.query.console.command.AbstractCommandTest;

public class TestOpenFileCommand extends AbstractCommandTest {

    @Override
    protected void setupCommand() {
        command = new OpenFileCommand();
        try {
            final PrintWriter fileOut = new PrintWriter("out.slql");
            fileOut.append("select *;");
            fileOut.flush();
            fileOut.close();
        } catch (final FileNotFoundException e) {}
    }

    @After
    public void deleteTestFile() {
        new File("out.slql").delete();
    }

    @Test
    public void testAcceptInValidParameter() {
        final ConsoleState state = new ConsoleState(null);
        state.setInput("get ");

        assertThat(command.accept(state), is(false));
    }

    @Test
    public void testAcceptInValidParameter2() {
        final ConsoleState state = new ConsoleState(null);
        state.setInput("getx property");

        assertThat(command.accept(state), is(false));
    }

    @Test
    public void testAcceptInValidParameter3() {
        final ConsoleState state = new ConsoleState(null);
        state.setInput("add get");

        assertThat(command.accept(state), is(false));
    }

    @Test
    public void testAcceptInValidParameter4() {
        final ConsoleState state = new ConsoleState(null);
        state.setInput("getx property ");

        assertThat(command.accept(state), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcceptNull() {
        assertThat(command.accept(null), is(false));
    }

    @Test
    public void testAcceptNullInout() {
        final ConsoleState state = new ConsoleState(null);
        state.setInput(null);

        assertThat(command.accept(state), is(false));
    }

    @Test
    public void testAcceptValidParameter() {
        final ConsoleState state = new ConsoleState(null);
        state.setInput("get filename.slql");

        assertThat(command.accept(state), is(true));
    }

    @Test
    public void testAcceptValidParameter2() {
        final ConsoleState state = new ConsoleState(null);
        state.setInput("get something");

        assertThat(command.accept(state), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteNull() {
        command.execute(null, null, null);
    }

    @Test
    public void testValidParameter() {
        final ConsoleState state = new ConsoleState(null);
        state.setInput("get filename.slql");
        state.appendBuffer("something");

        command.execute(reader, out, state);

        assertThat(state.getBuffer().length(), is(0));
        assertThat(state.getLastQuery().length(), is(0));
        assertThat(state.getInput(), is(""));
    }

    @Test
    public void testValidParameter2() {
        final ConsoleState state = new ConsoleState(null);
        state.setInput("get out.slql");
        state.appendBuffer("something");

        command.execute(reader, out, state);

        assertThat(state.getBuffer().length(), is(0));
        assertThat(state.getLastQuery(), is("select *;\n"));
        assertThat(state.getInput(), is(""));
    }

}
