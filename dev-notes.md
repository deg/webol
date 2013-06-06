Framework setup
===============

Dependencies
------------

- See [get-started-with-clojurescript-with-leiningen-templates
  ](http://squirrel.pl/blog/2013/01/02/get-started-with-clojurescript-with-leiningen-templates/)
  and [cljs-kickoff](https://github.com/konrad-garus/cljs-kickoff).

- Options for project.clj are in
  [sample.project.clj](https://github.com/emezeske/lein-cljsbuild/blob/0.3.0/sample.project.clj) from
  [lein-cljsbuild](https://github.com/emezeske/lein-cljsbuild).

Launch
------

- *In one shell:* `lein ring server-headless`
- *In second shell:* `lein cljsbuild auto`
- <del>`lein trampoline cljsbuild repl-listen`</del>
- *In emacs:*
  - *In .clj file:* `F8 (M-x nrepl-jack-in)`
  - *In .clj buffer:* `c-C c-K   c-C m-N`
  - *In *nrepl* buffer:* `(dev/r15)`  *(not strictly needed)*
  - *In *nrepl* buffer:* `(require 'cljs.repl.browser)`
  - *In *nrepl* buffer:* `(cemerick.piggieback/cljs-repl
			:repl-env (doto (cljs.repl.browser/repl-env :port 9000)
					cljs.repl/-setup))`
  - *In .cljs buffer:* `c-C c-K   c-C m-N`
  - *In .clj file:* `F8 (M-x nrepl-jack-in)`
  - *In .clj buffer:* `c-C c-K   c-C m-N`
  - *In *nrepl* buffer:* `(dev/r15)`  *(not strictly needed)*
- Still trying to figure out rest of repl+CLJS config stuff, but many tantalizing hints in
  [piggieback](https://github.com/cemerick/piggieback#piggieback-),
  [NRepl](https://github.com/kingtim/nrepl.el#installation), and
  [pedestal](http://dykcode.tumblr.com/post/50119528927/pedestal-cljs-emacs-repl-workflow-using-piggieback)
  notes.


Heroku notes
------------
- First server is free. Limited to 500MB. Suspended when idle for 60 minutes
- Keeping alive via cron job (crontab -e):
    15 * * * * curl http://receipts.goldfarb-family.com
    37 * * * * curl http://receipts.goldfarb-family.com
- Memory monitoring currently off, because too verbose in the logs. Enable with
  heroku labs:enable log-runtime-metrics
  See [log-runtime-metrics](https://devcenter.heroku.com/articles/log-runtime-metrics)


Tools
-----
[SdbNavigator
](https://chrome.google.com/webstore/detail/sdbnavigator/ddhigekdfabonefhiildaiccafacphgg?hl=en)
in the Chrome web store. Simple, but effective and working, interface to AWS SimpleDB.


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
- Email receipts
- Record currency; default to NIS; also USD, Euro, etc.
- Have list of valid credit cards, etc.
- Mode switch, based on country (use location data to set default?)
  - Change credit cards
  - Change currency

