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


To-do (features)
----------------
- Current date in date field
- Store receipts in a file
- Email receipts
- Deploy to AWS

