(ns example.events
  (:require [re-frame.core :as re-frame]
            [example.db :as db]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

(re-frame/reg-event-db
  ::initialize-db
  (fn-traced [_ _]
    db/default-db))

(re-frame/reg-event-db ::change-form-state
  (fn [db _]
    (assoc-in db [:someform :a] (rand-int 20))))

(re-frame/reg-event-db ::set-someform
  (fn [db [_ contents]]
    (assoc db :someform contents)))
