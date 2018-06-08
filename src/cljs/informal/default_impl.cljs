(ns informal.default-impl
  (:require [informal.common :as common]))

(defn error-view [error]
  (let [err @error]
    (when err
      [:div {:style {:font-size "60%"
                     :margin-top 5
                     :color "red"}}
       err])))

(defn text [{:keys [field value label error params]}]
  [:form {:key field}
   [:label {:for field
            :style {} } label]
   [:br]
   [:input (merge params {:value @value
                          :id field
                          :on-change #(reset! value (common/event->value %))})]
   [error-view error]])

(defn number [{:keys [field value label error params]}]
  [:form {:key field}
   [:label {:for field
            :style {}} label]
   [:br]
   [:input (merge params {:value @value
                          :id field
                          :type "number"
                          :on-change #(reset! value (common/event->value %))})]
   [error-view error]])

(defn dropdown-option [default-value [value label]]
  [:option {:value value
            :key value}
   label])

(defn select [{:keys [value error values params field label]}]
  [:form {:key field}
   [:label {:for field
            :style {:margin 10}} label]
   [:br]
   [:select (merge params {:id field
                           :value @value
                           :on-change #(reset! value (common/event->value %))})
    (doall (map (partial dropdown-option @value) values))]
   [error-view error]])

(defn checkbox [{:keys [value params error label field]}]
  [:form {:key field}
   [:label {:for field
            :style {:margin 10}} label]
   [:br]
   [:input {:type :checkbox
            :value @value
            :on-change #(reset! value (common/event->checked %))}]
   [error-view error]])

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

(defn form-layout [form-params title fields buttons]
  [:div {:id (:id form-params)
         :key :content
         :style {:display :flex
                 :width (or (-> form-params :params :width) 300)
                 :flex-direction :column}}
   title
   fields
   buttons])

(defn dialog-layout [params title fields buttons]
  [form-layout (:id params) fields])

(defn required-validator [value]
  (when (empty? value) "Should not be empty"))

(def *default-impl* {:form/text {:render #'text}
                     :form/text* {:render #'text
                                  :validator #'required-validator}
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
