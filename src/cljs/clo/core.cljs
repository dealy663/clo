(ns clo.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [clo.ajax :refer [load-interceptors!]]
            [ajax.core :refer [GET POST]]
            [clo.components.sidebar :as sbar]
            [clo.components.common :as c]
            [clo.components.registration :as reg])
  (:import goog.History))

(defn user-menu []
  (if-let [id (session/get :identity)]
    [:ul.nav.navbar-nav.pull-xs-right
     [:li.nav-item
      [:a.dropdown-item.btn
       {:on-click #(session/remove! :identity)}
       [:i.fa.fa-user] " " id " | sign out"]]]
    [:ul.nav.navbar-nav.pull-xs-right
     [:li.nav-item [reg/registration-button]]]))

(defn nav-link [uri title page collapsed?]
  [:li.nav-item
   {:class (when (= page (session/get :page)) "active")}
   [:a.nav-link
    {:href uri
     :on-click #(reset! collapsed? true)} title]])

(defn navbar []
  (let [collapsed? (r/atom true)]
    (fn []
      [:nav.navbar.navbar-light.bg-faded
       [:button.navbar-toggler.hidden-sm-up
        {:on-click #(swap! collapsed? not)} "☰"]
       [:div.collapse.navbar-toggleable-xs
        (when-not @collapsed? {:class "in"})
        [:a.navbar-brand {:href "#/"} "Ask" [:em "Clo"]]
        [:ul.nav.navbar-nav
         [nav-link "#/" "Home" :home collapsed?]
         [nav-link "#/about" "About" :about collapsed?]]]
       [user-menu]])))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     "this is the story of clo... work in progress"]]])

;(defn home-page []
;  [:div.container
;   [:div.jumbotron
;    [:h1 "Welcome to clo"]
;    [:p "Time to start building your site!"]
;    [:p [:a.btn.btn-primary.btn-lg {:href "http://luminusweb.net"} "Learn more »"]]]
;   [:div.row
;    [:div.col-md-12
;     [:h2 "Welcome to ClojureScript"]]]
;   (when-let [docs (session/get :docs)]
;     [:div.row
;      [:div.col-md-12
;       [:div {:dangerouslySetInnerHTML
;              {:__html (md->html docs)}}]]])])

(defn home []
  [:div#wrapper.toggled
   [sbar/sidebar]
   [:div.page-content-wrapper>div.container-fluid>div.row>div.col-lg-12
    [sbar/menu-toggle]]])

(def pages
  {:home #'home
   :about #'about-page})

;(defn page []
;  [(pages (session/get :page))])

;(defn page []
;  [:div
;   [(pages (session/get :page))]])

(defn modal []
  (when-let [session-modal (session/get :modal)]
    [session-modal]))

(defn page []
  [:div
   [modal]
   [(pages (session/get :page))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/about" []
  (session/put! :page :about))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          HistoryEventType/NAVIGATE
          (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (load-interceptors!)
  (hook-browser-navigation!)
  (mount-components))
