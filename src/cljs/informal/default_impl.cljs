(ns informal.default-impl
  (:require [informal.common :as common]))

(defn text [{:keys [field value label error params]}]
  (.log js/console @error)
  [:form {:key field}
   [:label {:for field
            :style {} } label]
   [:br]
   [:input (merge params {:value @value
                          :id field
                          :on-change #(reset! value (common/event->value %))})]
   (when @error
     [:div {:style {:font-size "60%"
                    :margin-top 5
                    :color "red"}}
      @error])])

(defn number [{:keys [field value label error params]}]
  [:form {:key field}
   [:label {:for field
            :style {}} label]
   [:br]
   [:input (merge params {:value @value
                          :id field
                          :type "number"
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

(defn buttons-layout [{:keys [impl custom-buttons] :as params} save-button cancel-button]
  [:div {:style {:margin-top "1em"
                 :padding-right "1em"
                 :display :flex
                 :justify-content :flex-end}}
   (seq custom-buttons)
   [cancel-button params]
   [save-button params]])

(defn form-layout [id & fields]
  [:div {:id id
         :key :content
         :style {:display :flex
                 :flex-direction :column}} fields])

(defn text-required-validator [text]
  (when (empty? text) "Should not be empty"))

(def *default-impl* {:form/text {:render #'text}
                     :form/text* {:render #'text
                                  :validator #'text-required-validator}
                     :form/select {:render #'select}
                     :form/number {:render #'number}
                     :form/checkbox {:render #'checkbox}
                     :buttons-layout {:render #'buttons-layout}
                     :form-layout {:render #'form-layout}
                     :dialog-layout {:render (fn [id {:keys [save-button cancel-button custom-buttons] :as buttons} & fields]
                                               [:div fields])}
                     :form-title {:render (fn [title] [:h2 title])}
                     :save-button {:render #'save-button}
                     :cancel-button {:render #'cancel-button}})
(def *impl* {})
