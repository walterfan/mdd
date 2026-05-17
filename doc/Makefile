# Minimal makefile for Sphinx documentation
#

# You can set these variables from the command line, and also
# from the environment for the first two.
SPHINXOPTS    ?=
POETRY        ?= poetry
SPHINXBUILD   ?= $(POETRY) run sphinx-build
SPHINXAUTOBUILD ?= $(POETRY) run sphinx-autobuild
SOURCEDIR     = source
BUILDDIR      = build
GITHUB_PAGES_REMOTE ?= origin
GITHUB_PAGES_BRANCH ?= gh-pages
GITHUB_PAGES_WORKTREE ?= $(BUILDDIR)/$(GITHUB_PAGES_BRANCH)

# Put it first so that "make" without argument is like "make help".
help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

.PHONY: help Makefile setup install html livehtml clean publish

# Convenience targets
setup:
	$(POETRY) install

install: setup

html:
	@$(SPHINXBUILD) -b html -E "$(SOURCEDIR)" "$(BUILDDIR)/html" $(SPHINXOPTS) $(O)

livehtml:
	$(SPHINXAUTOBUILD) "$(SOURCEDIR)" "$(BUILDDIR)/html" $(SPHINXOPTS) $(O)

clean:
	rm -rf $(BUILDDIR)/*

publish: html
	@set -e; \
	touch "$(BUILDDIR)/html/.nojekyll"; \
	git worktree remove --force "$(GITHUB_PAGES_WORKTREE)" >/dev/null 2>&1 || rm -rf "$(GITHUB_PAGES_WORKTREE)"; \
	if git ls-remote --exit-code --heads "$(GITHUB_PAGES_REMOTE)" "$(GITHUB_PAGES_BRANCH)" >/dev/null 2>&1; then \
		git fetch "$(GITHUB_PAGES_REMOTE)" "$(GITHUB_PAGES_BRANCH)"; \
		git worktree add --force "$(GITHUB_PAGES_WORKTREE)" "$(GITHUB_PAGES_REMOTE)/$(GITHUB_PAGES_BRANCH)"; \
	else \
		git worktree add --force --detach "$(GITHUB_PAGES_WORKTREE)" HEAD; \
		git -C "$(GITHUB_PAGES_WORKTREE)" checkout --orphan "$(GITHUB_PAGES_BRANCH)"; \
		git -C "$(GITHUB_PAGES_WORKTREE)" rm -rf . >/dev/null 2>&1 || true; \
	fi; \
	trap 'git worktree remove --force "$(GITHUB_PAGES_WORKTREE)" >/dev/null 2>&1 || true' EXIT; \
	rsync -a --delete --exclude=".git" "$(BUILDDIR)/html/" "$(GITHUB_PAGES_WORKTREE)/"; \
	git -C "$(GITHUB_PAGES_WORKTREE)" add -A; \
	if git -C "$(GITHUB_PAGES_WORKTREE)" diff --cached --quiet; then \
		echo "No documentation changes to publish."; \
	else \
		git -C "$(GITHUB_PAGES_WORKTREE)" commit -m "Publish documentation"; \
		git -C "$(GITHUB_PAGES_WORKTREE)" push "$(GITHUB_PAGES_REMOTE)" "HEAD:$(GITHUB_PAGES_BRANCH)"; \
	fi

# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile
	@$(SPHINXBUILD) -M $@ "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)
