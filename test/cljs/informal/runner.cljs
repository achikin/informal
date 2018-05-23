(ns informal.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [informal.core-test]))

(doo-tests 'informal.core-test)
