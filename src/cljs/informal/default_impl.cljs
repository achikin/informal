(ns informal.default-impl
  (:require [informal.common :as common]))

(defn text [{:keys [field value label error params]}]
  [:form {:key field}
   [:label {:for field
            :style {:margin 10}} label]
   [:input (merge params {:value @value
                          :id field
                          :on-change #(reset! value (common/event->value %))})]])

(defn dropdown-option [default-value [value label]]
  [:option {:value value
            :key value}
   label])

(defn select [{:keys [value error values params field label]}]
  [:form {:key field}
   [:label {:for field
            :style {:margin 10}} label]
   [:select (merge params {:id field
                           :value @value
                           :on-change #(reset! value (common/event->value %))})
    (doall (map (partial dropdown-option @value) values))]])

(defn checkbox [{:keys [value params label field]}]
  [:form {:key field}
   [:label {:for field
            :style {:margin 10}} label]
   [:input {:type :checkbox
            :value @value
            :on-change #(reset! value (common/event->checked %))}]])

(defn save-button [{:keys [label on-click disabled]}]
  [:button {:on-click on-click
            :disabled disabled}
   label])

(defn cancel-button [{:keys [label on-click]}]
  [:button {:on-click on-click} label])

(def *default-impl* {:form/text {:render #'text}
                     :form/select {:render #'select}
                     :form/checkbox {:render #'checkbox}
                     :form-layout {:render (fn [id & fields]
                                             [:div {:id id
                                                    :key :content
                                                    :style {:display :flex
                                                            :flex-direction :column}} fields])}
                     :dialog-layout {:render (fn [id {:keys [save-button cancel-button custom-buttons] :as buttons} & fields]
                                               [:div fields])}
                     :form-title {:render (fn [title] [:h2 title])}
                     :save-button {:render #'save-button}
                     :cancel-button {:render #'cancel-button}})
(def *impl* {})
