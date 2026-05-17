# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
project = 'Metrics Driven Development'
copyright = '2020-2026, Walter Fan, Jian Fu'
author = 'Walter Fan, Jian Fu'
release = '2.0'
version = '2.0.0'

# -- General configuration ---------------------------------------------------
extensions = [
    'sphinx.ext.autodoc',
    'sphinx.ext.viewcode',
    'sphinx.ext.todo',
    'sphinx.ext.graphviz',
    'sphinx.ext.ifconfig',
    'sphinx.ext.intersphinx',
]

templates_path = ['_templates']
exclude_patterns = []

# Support both Chinese and English
language = 'zh_CN'

# -- Options for HTML output -------------------------------------------------
html_theme = 'sphinx_book_theme'
html_static_path = []

html_theme_options = {
    'repository_url': 'https://github.com/walterfan/mdd',
    'path_to_docs': 'doc/source',
    'use_repository_button': True,
    'use_edit_page_button': True,
    'use_issues_button': True,
    'show_navbar_depth': 2,
    'show_toc_level': 2,
}

html_title = '度量驱动开发 - Metrics Driven Development'
html_short_title = 'MDD'
html_logo = None
html_favicon = None

# -- Options for LaTeX output ------------------------------------------------
latex_engine = 'xelatex'
latex_elements = {
    'papersize': 'a4paper',
    'pointsize': '11pt',
    'preamble': r'''
\usepackage{xeCJK}
\setCJKmainfont{Songti SC}
\setCJKsansfont{PingFang SC}
\setCJKmonofont{STFangsong}
''',
}

latex_documents = [
    ('index', 'MDD.tex', 'Metrics Driven Development',
     'Walter Fan, Jian Fu', 'manual'),
]

# -- Options for EPUB output -------------------------------------------------
epub_title = project
epub_author = author
epub_publisher = author
epub_copyright = copyright

# -- Extension configuration -------------------------------------------------
todo_include_todos = True

# Intersphinx mapping
intersphinx_mapping = {
    'python': ('https://docs.python.org/3', None),
}

# -- Options for todo extension ----------------------------------------------
todo_include_todos = True
todo_emit_warnings = False
