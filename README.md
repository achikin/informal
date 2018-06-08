# Informal

*Would you like to sign my petition?*

Design-agnostic Reagent forms framework focused on DRY and maximum reusability

## Disclamer
*This framework does not provide ready-made forms. It rather serves as a mechamism to reduce boilerplate code in forms.*
## Features
- Compact and clean form definition
- Custom reusable form components
- Arbitrary form layout
- Validation and error rendering
- Custom save/cancel buttons with automatic state management
- Undo form changes
## Demo
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
                          :on-change #(on-change (common/event->value %)})]
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

## Documentation

### The `informal/form` function
```clojure
[informal/form param-map & components]
```
`param-map` can contain the following keys:

|param|description|
|---|---|
|`:state`|The initial state of your form. Usually just map, but whatever fits `reagent/create-cursor` can be passed here.|
|`:title`|Form title. Treated as string.|
|`:custom-buttons`|Sequence of additional buttons to be placed alongside with save/cancel button. See more (here)[#adding-custom-buttons].|
|`:save-title`, `:cancel-title`|Custom titles for save and cancel buttons|
|`:cancel-disabled?`|Set `true` to hide cancel button|
|`:dialog?`|Set `true` to render your form as a dialog ([see below](#layouts))|
|`:impl`|A set of rendering functions and validators ([see details below](#rolling-your-own-implementation))|

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
|param|description|
|-|-|
|`:field`|A corresponding key of this field's data in your state map. Could also be a vector of nested keys if you have map of maps. Technically it can be either key or anything that fits reagent/cursor path.|
|`:label`|String that contains label for your field. You can either provide it as a second parameter to your field like this `[:form/text :name "Name"]` or let Informal infer it from `:field`|
|`:value`|Derefable state of the current value of your field. Please do not try to set the value through this derefable. At the moment it's implemented as a cursor which could be set, but later things can change|
|`:on-change`|A function that you must use to report the updated value back to the form.|
|`:error`|Derefable that contains either `nil` or textual representation of an error, reported by validator.|
|`:values`|Additional data for `select` and other data-driven fields. You can pass it through third positional parameter to your component `[:form/select :city "Select city" [[:vrn "Voronezh"][:msk "Moscow"]]]` ([see more](#select-autocomplete-and-other-data-driven-fields))|
|`:params`|Param-map passed to your component as an optional first positional parameter `[:form/text {:param1 1} ...]`|
#### Select, autocomplete and other data-driven fields
Some of the fields like select fields need additional data to render choices or autocomplete options. You can pass that data to your field as a positional parameter after `label`:
```clojure
[:form/select :my-field "Please select something:" [[:id1 "Label1"] [:id2 "Label2"]]]
```
Conventionally that data is in the form of `[[value label] [value label] ...]` where `value` is what is actually assigned to your field and label is a textual representation of this value. But Informal does not process or alter that data so feel free to pass whatever you want here. E.g. you can pass a re-frame subscription or a vector of maps - it's up to you to decide.
Later on when creating your data-driven component that values will be accessible under a `:values` key in the param-map passed to your component:
```(defn select [{:keys [field value values <-- here ...]}] 
     ...)
```
#### Setting default implementation
Passing `:impl` to form every time could be very annoying so you can set the default impl for all your forms like this:
```clojure
(def impl {...})
(informal.form/set-default-impl! impl)
```
Later if you want to access your default impl you can find it inside the `informal.default-impl` namespace:
```clojure
(def default-impl (informal.default-impl/*impl*))
```
#### form-params map description
### Features
#### How it works?
Informal takes all the forms passed to `informal/form` function, traces them and replaces any tags found in `:impl` with an appropriate form field implementation, wraps those fields into form layout and adds save/cancel buttons. 
Also Informal captures passed state into an atom and then passes appropriate cursors to each field
So generally this
```
[informal/form {:state {:name "John"}
                :title "My form"
                :on-save #(...)}
  [:form/text :name]]
```
Becomes the equivalent of this
```
(defn my-form [params]
  (let [s (r/atom (:state params) ;;<-- captured state
        name (r/cursor s :name)]  ::<-- created cursor
    (fn [params]
      [:div ;;<-- wrapped form fields into layout
        [:input {:value @name ;;<-- replaced :form/text with appropriate :input form
                 :on-change #(reset! name %)]
        [:button {:on-click #((:params on-save) %)} "Save"])) ;;<--added save button
```
The code above is **not** the actual code behind the Informal.

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
  (let [state (:state form-params)
        apples (:apples @state)
        oranges (:oranges @state)]
    [:span params "Total apples and oranges: " (str (+ apples oranges))]))

[informal/form {:state {:apples 3, :oranges 4}}
  [:form/number :apples]
  [:form/number :oranges]
  [total {:style {:color :green}}]]
```
See `form-params` documentation (here)[]
#### Save/Cancel buttons
Informal manages `Save` and `Cancel` buttons disabled state by detecting changes in the form. In order to disable this behavior pass `:disable-save-toggle? true` to the `informal/form` function:
```clojure
[informal/form {...
                :disable-save-toggle? true
                ...}
  ...]
```
#### Adding custom buttons
You can add any custom buttons to your form, passing a seq of components to your form using `:custom-buttons` key like this:
```clojure
[informal/form {....
                :custom-buttons [[:button "Delete"] [:button "Revert"]]
                ...}
  ...]
```
#### Validation

### Common cases
#### Working with re-frame
#### How do I access state?
#### Working with arrays of data
Sometimes you have data that contains arrays of the same-type information like this:
```
(def user {:name "John"
           :phones [{:number "123-456" :tag "mobile"}
                    {:number "12-85-00" :tag "home"}]})
```
And you want to be able to render that array-data inside your form and let user add/delete those entries.
The best solution is to roll your own implementation. That's what `:impl` is for!
```
Here goes an example with custom form field which renders an array and custom button which adds new element to array.
```
### Design philosophy
Informal percieves the following goals (prioritized top to bottom)
- Compact form definition
- Flexible form fields implementation
- Ease of components implementation
Exactly in this order. So, adopting Informal requires some work which makes Informal not suitable for drop-in form solution but gives a lot of advantages in the long run on big codebases:
- Forms become compact
- Form behavior is stable
- You can switch forms to the other UI framework by replacing one line of code


