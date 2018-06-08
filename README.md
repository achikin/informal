# informal

Design-agnostic Reagent forms framework focused on DRY and maximum reusability

## Disclamer
*This framework does not provide ready-made forms. It rather serves as a mechamism to reduce boilerplate code in forms.*

## Install
Add informal dependecy to the leiningen `project.clj`
```
[achikin/informal x.y.z]
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
(defn a [b]
  (c a))
```


## Documentation

### Initial setup
#### Rolling your own implementation
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



