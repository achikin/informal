(ns informal.form
  (:require [reagent.core :as r]
            [informal.default-impl :as default-impl]
            [informal.common :as common]))


(defn resolve-params
  "Extracts params from different hiccup forms into a map

  :field - key that represents field in the state map
  :label - field label. Either capitalized `:field` or contents of `:label` param
  :error - atom of error text, returned by validator
  :validator - validation function. Must return either error description or null
  :value - atom that contains field value
  :values - used by `select` and other fields, that require multiple values. Can be any derefable (atom,reaction,cursor)
  :form-params - WIP, ignore this for now
  :params - any params, passed via param-map of the component"

  [{:keys [state errors impl] :as form-params} tag params]
  (if (not (map? (first params)))
    (resolve-params form-params tag (cons {} params))
    (let [param-map (first params)
          field (second params)
          value (common/create-cursor state field)
          validator (or (:validator params) (-> impl tag :validator) #())
          error (common/create-cursor errors field)
          _ (when validator
              (add-watch value :validation
                (fn [a k old-val new-val] (reset! error (validator new-val)))))
          label (nth params 2 nil)
          values (nth params 3 nil)]
      {:field field
       :label (common/resolve-label field label)
       :error error
       :validator validator
       :value value
       :values values
       :form-params (select-keys param-map form-params)
       :params param-map})))

(defn resolve-field [form-params component]
  (let [tag (first component)
        params (resolve-params form-params tag (rest component))]
    ^{:key (:field params)}
    [(get-in form-params [:impl tag :render]) params]))

(declare resolve-components)

(defn resolve-tag [form-params field]
  (let [children (filter common/vector-or-seq field)
        tag (first field)
        params (when (map? (second field)) (second field))]
    (into [tag params] (resolve-components form-params children))))

(defn resolve-fn [form-params component]
  (let [children (filter common/vector-or-seq component)]
    (if-not (= 0 (count children))
      (resolve-tag form-params component)
      (conj component (:state form-params)))))

;;TODO trampoline this?

(defn resolve-components
  [form-params components]
  (for [c (common/realize-seqs components)]
    (let [tag (first c)]
      (cond
        ;;user-defined field from :impl
        (contains? (:impl form-params) tag)
        (resolve-field form-params c)

        ;; any arbitrary tag like :div :span or else
        (keyword? tag)
        (resolve-tag form-params c)

        ;; if we see function component - pass state to it as a last parameter
        ;; maybe change to IFn so that multimethods could be also used
        (fn? tag) (resolve-fn form-params c)

        :else (.error js/console "Form: can't resolve tag -> " tag)))))


(defn resolve-state [state]
  (cond
    (satisfies? IAtom state) state
    :else (r/atom state)))

(defn debug-state [state debug]
  (when debug
    (let [debug-fn (if (fn? debug) debug #(.log js/console %))]
      (add-watch state :debug
        (fn [a b old-val new-val] (debug-fn new-val))))))

(defn save-button [{:keys [impl params state changed] :as params}]
  [(-> impl :save-button :render) {:label (or (:save-label params) "Save")
                                   :on-click (fn [] (reset! changed false) ((:on-save params) @state))
                                   :key :save-button
                                   :disabled (not @changed)}])

(defn cancel-button [{:keys [impl state params]}]
  (when-not (:cancel-disabled? params)
    [(-> impl :cancel-button :render) {:label (or (:cancel-label params) "Cancel")
                                       :key :cancel-button
                                       :on-click #((:on-cancel params) @state)}]))

(defn form-buttons [params]
  [:div {:style {:margin-top "1em"
                 :padding-right "1em"
                 :display :flex
                 :justify-content :flex-end}}
   (seq (:custom-buttons params))
   [cancel-button params]
   [save-button params]])

(defn form [params & fields]
  (let [state (r/atom (:state params))
        changed? (r/atom false)
        errors (r/atom {})
        _ (add-watch state :changed (fn [_ _ _ _]
                                      (reset! changed? true)))]
    (r/create-class
      {:display-name "informal/form"

       :should-component-update (fn [this [_ old-params] [_ new-params]]
                                  (let [new-st (:state new-params)
                                        old-st (:state old-params)
                                        other-new (dissoc new-params :state)
                                        other-old (dissoc old-params :state)]
                                    (or
                                      (when-not (= old-st new-st)
                                        (reset! state new-st)
                                        (reset! errors {})
                                        (reset! changed? false)
                                        true)
                                      (not= other-old other-new))))

       :component-will-unmount #(do
                                  (remove-watch state :debug)
                                  (remove-watch state :changed))

       :reagent-render (fn [params & fields]
                         (let [_ (when (:debug params) (debug-state state (:debug params)))
                               impl (or (:impl params) default-impl/*impl*)
                               form-params {:state state
                                            :params (dissoc params :state :debug :impl)
                                            :errors errors
                                            :changed changed?
                                            :impl impl}
                               resolved-fields (resolve-components form-params fields)]
                           (if (:dialog? params)
                             [(-> impl :dialog-layout :render) form-params resolved-fields]
                             [(-> impl :form-layout :render)
                              form-params
                              (when-let [title (-> form-params :params :title)]
                                [(-> form-params :impl :form-title :render) title])
                              resolved-fields
                              [(-> impl :buttons-layout :render)
                               form-params save-button cancel-button]])))})))


(defn set-default-impl! [impl]
  (set! default-impl/*impl* impl))

(set-default-impl! default-impl/default-impl)
