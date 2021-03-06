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
package org.openspotlight.bundle.language.java.bundle;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;
import org.openspotlight.bundle.common.metrics.SourceLineInfoAggregator;
import org.openspotlight.bundle.common.parser.SLArtifactStream;
import org.openspotlight.bundle.language.java.parser.JavaLexer;
import org.openspotlight.bundle.language.java.parser.JavaParser;
import org.openspotlight.bundle.language.java.parser.JavaPublicElementsTree;
import org.openspotlight.bundle.language.java.parser.executor.JavaExecutorSupport;
import org.openspotlight.bundle.language.java.parser.executor.JavaLexerExecutor;
import org.openspotlight.bundle.language.java.parser.executor.JavaParserExecutor;
import org.openspotlight.common.util.Assertions;

/**
 * This is just a dto. This bunch of builders are used to ensure that the dto will have only final fields, since it will be shared
 * between threads.
 * 
 * @author feu
 */
public class JavaTransientDto {

    private abstract static class BuilderParameters {

        protected SLArtifactStream         stream;

        protected JavaLexer                lexer;

        protected SourceLineInfoAggregator sourceLine;
        protected JavaLexerExecutor        lexerExecutor;
        protected CommonTokenStream        commonTokenStream;
        protected JavaParserExecutor       parserExecutor;
        protected JavaExecutorSupport      support;

        protected JavaParser               parser;
        protected Tree                     tree;
        protected CommonTreeNodeStream     treeNodes;
        protected JavaPublicElementsTree   walker;

        BuilderParameters() {

        }

        BuilderParameters(
                           final JavaTransientDto dto ) {
            stream = dto.stream;
            lexer = dto.lexer;
            sourceLine = dto.sourceLine;
            lexerExecutor = dto.lexerExecutor;
            commonTokenStream = dto.commonTokenStream;
            parserExecutor = dto.parserExecutor;
            parser = dto.parser;
            tree = dto.tree;
            treeNodes = dto.treeNodes;
            walker = dto.walker;
        }

        public final JavaTransientDto create() {
            validate();
            return new JavaTransientDto(stream, lexer, sourceLine, lexerExecutor, commonTokenStream, parser, parserExecutor,
                                        tree, treeNodes, walker, support);
        }

        protected abstract void validate();

    }

    public static class ParserDtoBuilder extends BuilderParameters {

        private ParserDtoBuilder() {
            super();
        }

        @Override
        protected void validate() {
            Assertions.checkNotNull("tree", tree);
            Assertions.checkNotNull("parserExecutor", parserExecutor);
            Assertions.checkNotNull("parser", parser);
            Assertions.checkNotNull("stream", stream);
            Assertions.checkNotNull("lexer", lexer);
            Assertions.checkNotNull("sourceLine", sourceLine);
            Assertions.checkNotNull("lexerExecutor", lexerExecutor);
            Assertions.checkNotNull("commonTokenStream", commonTokenStream);

        }

        public ParserDtoBuilder withCommonTokenStream( final CommonTokenStream commonTokenStream ) {
            this.commonTokenStream = commonTokenStream;
            return this;
        }

        public ParserDtoBuilder withLexer( final JavaLexer lexer ) {
            this.lexer = lexer;
            return this;
        }

        public ParserDtoBuilder withLexerExecutor( final JavaLexerExecutor lexerExecutor ) {
            this.lexerExecutor = lexerExecutor;
            return this;
        }

        public ParserDtoBuilder withParser( final JavaParser parser ) {
            this.parser = parser;
            return this;
        }

        public ParserDtoBuilder withParserExecutor( final JavaParserExecutor parserExecutor ) {
            this.parserExecutor = parserExecutor;
            return this;
        }

        public ParserDtoBuilder withSourceline( final SourceLineInfoAggregator sourceLine ) {
            this.sourceLine = sourceLine;
            return this;
        }

        public ParserDtoBuilder withStream( final SLArtifactStream stream ) {
            this.stream = stream;
            return this;
        }

        public ParserDtoBuilder withTree( final Tree tree ) {
            this.tree = tree;
            return this;
        }

    }

    public static class TreeDtoBuilder extends BuilderParameters {

        TreeDtoBuilder(
                        final JavaTransientDto dto ) {
            super(dto);
        }

        @Override
        protected void validate() {
            Assertions.checkNotNull("treeNodes", treeNodes);
            Assertions.checkNotNull("walker", walker);
            Assertions.checkNotNull("tree", tree);
            Assertions.checkNotNull("parserExecutor", parserExecutor);
            Assertions.checkNotNull("parser", parser);
            Assertions.checkNotNull("stream", stream);
            Assertions.checkNotNull("lexer", lexer);
            Assertions.checkNotNull("sourceLine", sourceLine);
            Assertions.checkNotNull("lexerExecutor", lexerExecutor);
            Assertions.checkNotNull("commonTokenStream", commonTokenStream);
            Assertions.checkNotNull("support", support);

        }

        public TreeDtoBuilder withExecutorSupport( final JavaExecutorSupport support ) {
            this.support = support;
            return this;
        }

        public TreeDtoBuilder withTreeNodeStream( final CommonTreeNodeStream treeNodes ) {
            this.treeNodes = treeNodes;
            return this;
        }

        public TreeDtoBuilder withWalker( final JavaPublicElementsTree walker ) {
            this.walker = walker;
            return this;
        }

    }

    public static ParserDtoBuilder fromParser() {
        return new ParserDtoBuilder();
    }

    public static TreeDtoBuilder fromTree( final JavaTransientDto dto ) {
        return new TreeDtoBuilder(dto);
    }

    public final JavaExecutorSupport      support;
    public final SLArtifactStream         stream;
    public final JavaLexer                lexer;
    public final SourceLineInfoAggregator sourceLine;
    public final JavaLexerExecutor        lexerExecutor;
    public final CommonTokenStream        commonTokenStream;
    public final JavaParser               parser;
    public final JavaParserExecutor       parserExecutor;
    public final Tree                     tree;
    public final CommonTreeNodeStream     treeNodes;
    public final JavaPublicElementsTree   walker;

    private JavaTransientDto(
                              final SLArtifactStream stream, final JavaLexer lexer, final SourceLineInfoAggregator sourceLine,
                              final JavaLexerExecutor lexerExecutor, final CommonTokenStream commonTokenStream,
                              final JavaParser parser, final JavaParserExecutor parserExecutor, final Tree tree,
                              final CommonTreeNodeStream treeNodes, final JavaPublicElementsTree walker,
                              final JavaExecutorSupport support ) {
        super();
        this.support = support;
        this.stream = stream;
        this.lexer = lexer;
        this.sourceLine = sourceLine;
        this.lexerExecutor = lexerExecutor;
        this.commonTokenStream = commonTokenStream;
        this.parser = parser;
        this.parserExecutor = parserExecutor;
        this.tree = tree;
        this.treeNodes = treeNodes;
        this.walker = walker;
    }

}
