(ns visuals.javafx.toolkit
  "JavaFX implementations of core abstractions"
  (:import [javafx.application Platform]
           [javafx.event ActionEvent]
           [javafx.scene Node]
           [javafx.stage Stage WindowEvent]
           [org.tbee.javafx.scene.layout MigPane])
  (:require [visuals.javafx.builder :as builder]
            [visuals.javafx.application]
            [metam.core :as m])
  (:use [visuals core utils]
        [visuals.javafx utils]))

;; Event translation

(defmulti translate
  "Takes a JavaFX event and returns an event map."
  (fn [comp-map evt]
    (class evt)))

(defn- make-event
  "Returns a vector of component names conjoined with an action key, 
  for example [\"Preview: Content\" \"Actions\" \"Add Item\" :action]"
  [comp-map evt actionkey]
  (let [component (.getSource evt)
        comp-path (->> comp-map
                       (filter #(= component (second %)))
                       ffirst
                       vec)]
    (event (conj comp-path actionkey) nil nil)))


(defmethod translate :default
  [comp-map evt]
  (make-event comp-map evt :unknown))


(defmethod translate ActionEvent
  [comp-map evt]
  (make-event comp-map evt :action))


(defmethod translate WindowEvent
  [comp-map evt]
  (make-event comp-map evt
               (condp = (.getEventType evt)
                 WindowEvent/WINDOW_CLOSE_REQUEST :close
                 WindowEvent/WINDOW_SHOWN :shown
                 WindowEvent/WINDOW_HIDDEN :hidden
                 WindowEvent/WINDOW_SHOWING :showing
                 WindowEvent/WINDOW_HIDING :hiding)))


;; Toolkit implementation for JavaFX

(defrecord JfxToolkit []
  Toolkit
  (build* [tk spec] (builder/build spec))
  (show!* [tk vc] (.show vc) (when (instance? Stage vc) (.sizeToScene vc)))
  (hide!* [tk vc] (.hide vc))
  (translate-event [tk comp-map event] (translate comp-map event))
  (run-later*
    [tk f]
    (launch-if-necessary)
    (Platform/runLater f)))



(extend-type Node
  VisualComponent
  (compname [vc] (.getId vc))
  (comptype [vc] (-> vc (getp :spec) m/metatype))
  (parent [vc] (.getParent vc))
  (set-error! [vc on] (.setStyle vc (if on "-fx-border-color: red;" "-fx-border-color: white;")))
  (children [vc] (if (instance? MigPane vc) (or (.getChildren vc) []) []))
  (eventsources [vc] (getp vc :eventsources))
  (signals [vc] (getp vc :signals)))


(extend-type Stage
  VisualComponent
  (compname [vc] (-> vc (getp :spec) :name))
  (comptype [vc] (-> vc (getp :spec) m/metatype))
  (parent [vc] (.getOwner vc))
  (set-error! [vc on] on)
  (children [vc] [(-> vc .getScene .getRoot)])
  (eventsources [vc] (getp vc :eventsources))
  (signals [vc] (getp vc :signals)))




