Framework setup
===============

Dependencies
------------

- See http://squirrel.pl/blog/2013/01/02/get-started-with-clojurescript-with-leiningen-templates/
  and https://github.com/konrad-garus/cljs-kickoff.

- Options for project.clj are in
  https://github.com/emezeske/lein-cljsbuild/blob/0.3.0/sample.project.clj from
  https://github.com/emezeske/lein-cljsbuild

Launch
------

- lein ring server-headless
- lein cljsbuild auto
- (doesn't work for me -- need to compare to tutorial project) lein trampoline cljsbuild repl-listen


To-do (dev)
-----------
- Learn and use firebug, or find chrome equivalent
- Cleanup namespaces and directory structure
- Stop passing around all fields everywhere. Pass a map instead.
- Learn how to write unit-tests for CLJS/CLJ
- Learn how to use full NREPL tools for CLSJ/CLJ dev
  - Emacs compiling into repl of ring server
  - Emacs compiling into javascript, with minimal page reloads needed for testing


To-do (features)
----------------
- Current date in date field
- Store receipts in a file
- Email receipts
- Record currency; default to NIS; also USD, Euro, etc.
- Have list of valid credit cards, etc.
- Mode switch, based on country (use location data to set default?)
  - Change credit cards
  - Change currency
- Deploy to AWS
- Basic security: Client must have a token in order to enter (or view) data

