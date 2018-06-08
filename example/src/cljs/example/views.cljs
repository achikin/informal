(ns example.views
  (:require [re-frame.core :as rf]
            [example.subs :as subs]
            [informal.form :as form]
            [informal.default-impl :as impl]
            [reagent.core :as r]))

(defonce state (r/atom {}))


(defn main-panel []
  (let [name (rf/subscribe [::subs/name])]
    [:div
     [:div (str @state)]
     [:button
      {:on-click #(reset! state {:num (rand-int 20)})}
      "Change atom"]
     [:button {:on-click #(rf/dispatch [:example.events/change-form-state])}
      "Change someform state"]
     [form/form {:state @(rf/subscribe [:example.subs/someform])
                 :title "Someform"
                 :on-save #(rf/dispatch [:example.events/set-someform %])
                 :impl impl/*default-impl*}
      [:form/number :a]
      [:form/text* :name]
      [:form/text :b]]]))
