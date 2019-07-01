(ns example.views
  (:require [re-frame.core :as rf]
            [example.subs :as subs]
            [informal.form :as form]
            [informal.default-impl :as default-impl]
            [reagent.core :as r]))

(defonce state (r/atom {}))


(defn myform []
  (let [state {:name "John"
               :last_name "Doe"
               :age 23}]
    [form/form {:state state
                :title "My shiny form"
                :on-save #(.log js/console %)
                :impl default-impl/default-impl}
     [:form/text :name]
     [:form/text* :last_name]
     [:form/number :age]]))


(defn main-panel []
  (let [name (rf/subscribe [::subs/name])]
    [:div
     [:div (str @state)]
     [:button
      {:on-click #(reset! state {:num (rand-int 20)})}
      "Change atom"]
     [:button {:on-click #(rf/dispatch [:example.events/change-form-state])}
      "Change someform state"]
     [myform]
     [form/form {:state @(rf/subscribe [:example.subs/someform])
                 :title "Someform"
                 :on-save #(rf/dispatch [:example.events/set-someform %])
                 :impl default-impl/default-impl}
      [:form/number :a]
      [:form/text* :name]
      [:form/text :b]]]))
