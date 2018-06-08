# Informal

> Would you like to sign my petition?

Design-agnostic Reagent forms framework focused on DRY and maximum reusability

## Disclamer
*This framework does not provide ready-made forms. It rather serves as a mechamism to reduce boilerplate code in forms.*

## Getting started
Add informal dependecy to the leiningen `project.clj`
```
[achikin/informal x.y.z]
```
Require `informal.form` in your views
```
(ns my-views
  (:require [informal.form :as form])
```
Roll your own implementation ([see details below](#rolling-your-own-implementation))
```clojure
(def impl {:form-layout {:render (fn [...] [:div ...])}
           :save-button {:render (fn [...] [:button "Save"])}
           :cancel-button {:render (fn [...] [:button "Cancel"])}
           :form/text {:render (fn [...] [:input ...])})
                       :validator #(when (empty? %) "Oops, empty!")}
```

Now define your views
```clojure
(defn my-form-view [state]
  [form/form {:state state
              :impl impl
              :on-save #(fn [state] (push-to-server state))}
    [:form/text :username]
    [:form/text :password]])
```
## Features
- Compact and clean form definition
- Custom reusable form components
- Arbitrary form layout
- Validation and error rendering
- Custom save/cancel buttons with automatic state management
- Undo form changes
### Simple demo
<img align="right" src="images/readme/informal4.gif?raw=true">

```clojure
(ns my-ns
    (:require [informal.form :as form]
              [informal.default-impl :as impl]))

(defn save-button [{:keys [label on-click disabled]}]
  [:button {:on-click on-click
            :disabled disabled}
   label])

(defn cancel-button [{:keys [label on-click]}]
  [:button {:on-click on-click} label])

(defn form-layout [form-params fields save-button cancel-button]
  [:div {:id (-> form-params :params :id)}
   [:h2  (-> form-params :params :title)]
   fields
   [:div
    (seq (-> form-params :params :custom-buttons))
    [cancel-button form-params]
    [save-button form-params]]])

(defn text [{:keys [field value label error params on-change]}]
  [:form {:key field}
   [:label {:for field
            :style {} } label]
   [:br]
   [:input (merge params {:value @value
                          :id field
                          :type "text"
                          :on-change on-change})]
   @error)

(def impl {:form-layout {:render #'form-layout}
           :save-button {:render #'save-button}
           :cancel-button {:render #'cancel-button}
           :form/text {:render #'text}})

(defn myform []
  (let [state {:name "John"
               :last_name "Doe"
               :age 23}]
    [form/form {:state state
                :title "My shiny form"
                :on-save #(.log js/console %)
                :impl impl/*default-impl*}
     [:form/text :name]
     [:form/text* :last_name]
     [:form/number :age]]))

```


## Documentation

### The `informal/form` function
```clojure
[informal/form param-map & components]
```
`param-map` can contain the following keys:
`:state` - the initial state of your form. Usually just map, but whatever fits `reagent/create-cursor` can be passed here.
`:title` - form title string
`:custom-buttons` - sequence of additional buttons to be placed alongside with save/cancel button
`:save-title`, `:cancel-title` - custom titles for save and cancel buttons
`:cancel-disabled?` - set `true` to hide cancel button
`:impl` - a set of rendering functions and validators ([see details below](#rolling-your-own-implementation))
### Rolling your own implementation
In order to stay non-opinionated and flexible Informal does not provide any ready-made components so you should provide implementations of your form fields by yourself. In order to do that you need to define form implementation and then pass it to your form.
```clojure
(def impl {...})
[informal/form {:state ...
                :impl impl}
  ...]
```
#### Layouts
#### Custom form fields
#### Select, autocomplete and other data-driven fields
#### Setting default implementation

### Features
#### How it works?
Informal takes all the parameters passed to it's `informal/form` function, traces them and replaces any tags found in `:impl` with an appropriate form field implementation.
#### Arbitrary layout
Informal detects form fields passed to any component, so you can insert any "layout" or enclosing tags inside your form
```clojure
[informal/form {:state {:name "" :age 20}}
  [:div {:style {:display :flex}}
    [:form/text :name]]
  [someframework/paper-view
    [:form/number :age]]]
```
But Informal does not look inside your custom components, so this won't work:
```clojure
(defn my-custom []
  [:form/text :name])

[informal/form {:state {:name ""}}
  [my-custom]]
```
#### Custom components inside form
As I said earlier - Informal does not look inside your custom components, but it passes form state and errors inside custom components as a first parameter so you can do some custom form state processing:
```clojure
(defn total [form-params params]
  (let [state (:state params)
        apples (:apples @state)
        oranges (:oranges @state)]
    [:span params "Total apples and oranges: " (str (+ apples oranges))]))

[informal/form {:state {:apples 3, :oranges 4}}
  [:form/number :apples]
  [:form/number :oranges]
  [total {:style {:color :green}}]]
```
#### Buttons
##### Adding custom buttons
#### Validation

### Common cases
#### Working with re-frame
#### How do I access state?



