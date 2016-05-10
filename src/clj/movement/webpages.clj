(ns movement.webpages
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [movement.activation :refer [generate-activation-id send-activation-email]]))

(defn html-head [title]
  [:head
   [:link {:rel "shortcut icon" :href "http://s3.amazonaws.com/mumrik-movement-images/static-air-baby.png"}]
   [:title title]
   [:script {:src "analytics.js" :type "text/javascript"}]
   (include-css
     "https://fonts.googleapis.com/css?family=Roboto"
     "https://fonts.googleapis.com/css?family=Raleway"
     "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css"
     "/css/pure-min.css"
     "/css/grids-responsive-min.css"
     "/css/normalize.css"
     "/css/marketing.css"
     "/css/site.css"
     "/css/pricing.css")
   [:meta {:name    "description"
           :content "Mumrik er en treningsdagbok, en personlig trener og et sosialt nettverk for trening"}]
   [:meta {:name    "google-site-verification"
           :content "4dEA4Y9dvxAtlRBwuG5bwlmo9fEKfI7TTX5wo4bdj_M"}]
   [:meta {:name    "msvalidate.01"
           :content "2B704D213DB4E877B4F06C35F4FADFC4"}]])

(defn footer []
  [:div.footer.l-box.center
   #_[:div#footer-links.center
      [:a {:title "Om Mumrik" :href "/about" :target "_top"} "Om"]
      [:a {:title "Kontakt oss" :href "/contact" :target "_top"} "Kontakt"]
      [:a {:title "Les bloggen" :href "/blog" :target "_top"} "Blogg"]]
   [:div#footer-logo.pure-g.center
    [:div.pure-u-1 [:img {:width 75 :height 75 :src "http://s3.amazonaws.com/mumrik-movement-images/static-air-baby.png"}]]]
   [:div#footer-copyright.pure-g.center
    [:div.pure-u-1 [:i.fa.fa-copyright] "2016 Mumrik"]]])

(defn landing-header []
  [:div.header
   [:div.home-menu.pure-menu.pure-menu-horizontal
    [:a.pure-menu-heading {:title  "Home"
                           :href   "/"
                           :target "_self"} "Mumrik"]
    [:ul.pure-menu-list
     [:li.pure-menu-item
      [:a.pure-menu-link {:title  "Sign up"
                          :href   "/signup"
                          :target "_self"} "Bli med"]]
     [:li.pure-menu-item
      [:a.pure-menu-link {:title  "Log in"
                          :href   "/app"
                          :target "_self"} "Logg inn"]]]]])

(defn epilog []
  [:div#epilog
   [:h2.content-head.center "So, are you ready to start moving more?"]
   [:div.pure-g
    [:div.pure-u-1.center
     [:a.m-button.orange.x-large.upper
      {:title "Sign Up" :href "/pricing" :target ""} "Try Movement Session"]
     [:a.l-m {:title "Learn more" :href "/tour" :target ""} "Or learn more"]]]])

(defn email-list []
  [:div.pure-g
   [:div.pure-u.pure-u-lg-1-5]
   [:div.pure-u-1.pure-u-lg-4-5
    [:link {:href "//cdn-images.mailchimp.com/embedcode/slim-10_7.css" :rel "stylesheet" :type "text/css"}]
    [:style {:type "text/css"} "#mc_embed_signup{background:#fff; clear:left; font:14px Helvetica,Arial,sans-serif; }"]
    [:div#mc_embed_signup
     [:form {:action "//movementsession.us12.list-manage.com/subscribe/post?u=82d2cd810b5590723731dc9a0&amp;id=e4ecd35054"
             :method "post" :id "mc-embedded-subscribe-form" :name "mc-embedded-subscribe-form" :class "validate"
             :target "_blank" :novalidate true}
      [:div#mc_embed_signup_scroll
       [:p {:for "mce-EMAIL"} "Subscribe to our mailing list to learn more, receive updates and future discounts"]
       [:input {:type "email" :value "" :name "EMAIL" :class "email" :id "mce-EMAIL" :placeholder "email address" :required true}]
       [:div {:style "position: absolute; left: -5000px;" :aria-hidden true}
        [:input {:type "text" :name "b_82d2cd810b5590723731dc9a0_e4ecd35054" :tab-index "-1" :value ""}]]
       [:div.clear [:input {:type "submit" :value "Subscribe" :name "subscribe" :id "mc-embedded-subscribe" :class "button"}]]]]]]])

(defn landing []
  [:div#splash
   [:div.pure-g
    [:div.pure-u-1-2
     [:div.pure-g
      [:h1.pure-u-1
       "Digital treningsdagbok"]]
     [:div.pure-g
      [:h1.pure-u-1
       "Mumrik lager tilpassede økter for nettopp deg"]]
     [:div.pure-g
      [:h1.pure-u-1
       "Skryt av det du gjør gjennom et sosialt nettverk"]]
     [:div.pure-g
      [:p.pure-u-1
       "Skap og loggfør tilpassede økter uten hjelp fra en personlig trener."]]
     [:div.pure-g
      [:div.pure-u-1.center
       [:button.m-button.orange.x-large.upper
        {:title "Sign Up" :href "/signup" :target ""} "Bli med helt gratis"]]]]
    [:div.pure-u-1-2
     [:img.pure-img-responsive {:src "images/mumrik.jpg"}]]]


   #_[:div#sell

    [:div.pure-g.l-box
     [:div.pure-u-1.pure-u-md-1-2
      [:div.pure-g
       [:p.pure-u-1 "a"]]
      [:div.pure-g
       [:p.pure-u-1 "a"]]]
     [:div.pure-u-1.pure-u-md-1-2
      [:img.pure-img-responsive {:src "images/session-in-feed.jpg"}]]]

    [:div.pure-g.l-box
     [:div.pure-u-1.pure-u-md-1-2
      [:img.pure-img-responsive {:src "images/session.jpg"}]]
     [:div.pure-u-1.pure-u-md-1-2
      [:div.pure-g
       [:p.pure-u-1 "a"]]
      [:div.pure-g
       [:p.pure-u-1 "a"]]]
     ]

    [:div.pure-g.l-box

     [:div.pure-u-1.pure-u-md-1-2
      [:div.pure-g
       [:p.pure-u-1 "a"]]
      [:div.pure-g
       [:p.pure-u-1 "a"]]]
     [:div.pure-u-1.pure-u-md-1-2
      [:img.pure-img-responsive {:src "images/session2.jpg"}]]
     ]

    [:div.pure-g.l-box
     [:div.pure-u-1.pure-u-md-1-2
      [:img.pure-img-responsive {:src "images/card.jpg"}]]
     [:div.pure-u-1.pure-u-md-1-2
      [:div.pure-g
       [:p.pure-u-1 "Å trykke på en øvelse utvider den og gir mulighet for justeringer."]]
      [:div.pure-g
       [:p.pure-u-1 "Du kan loggføre repetisjoner, avstand, tid brukt, vekt, hvilepauser og antall utførte sett."]]
      [:div.pure-g
       [:p.pure-u-1 "Det er også mulig å fjerne øvelser, bytte dem ut med noe lignende og å bytte en øvelse med en enklere eller vanskeligere variant."]]]
     ]

    [:div.pure-g.l-box

     [:div.pure-u-1.pure-u-md-1-2
      [:div.pure-g
       [:p.pure-u-1 "Mumrik er mest opptatt av kroppsvektøvelser, men han har også gjort klart noen vektede øvelser."]]]
     [:div.pure-u-1.pure-u-md-1-2
      [:img.pure-img-responsive {:src "images/weightlifting.jpg"}]]]]

   ])

(defn account-created [opts]
  [:div.l-box
   [:div.pure-g
    [:p.pure-u-1
     (str "For å bekrefte at eposten er din har vi sendt en epost til "
          (:email (first opts)) " med en bekreftelseslenke.")]]])

(defn account-exists [opts]
  [:div.l-box
   [:div.pure-g
    [:p.pure-u-1
     (str (:email (first opts)) " er allerede registrert.")]]])

(defn account-activated []
  [:div.l-box
   [:div.pure-g [:h3.pure-u-1.information-head "Kontoen er aktivert!"]]
   [:div.pure-g
    [:p.pure-u-1 "Du kan nå logge inn."]]])

(defn signup []
  [:div.pure-g
   [:div.pure-u.pure-u-md-1-5]
   [:div.pure-u-1.pure-u-md-3-5
    [:form.pure-form.pure-form-stacked
     {:method "POST"
      :action "/signup"}
     [:fieldset
      [:input#email.pure-input-1
       {:type        "email"
        :name        "email"
        :required    "required"
        :placeholder "Epost"}]
      [:input#username.pure-input-1
       {:type        "text"
        :name        "username"
        :required    "required"
        :placeholder "Brukernavn"}]
      [:input#password.pure-input-1
       {:type        "password"
        :name        "password"
        :placeholder "Passord"
        :required    "required"}]
      [:input.button.pure-input-1
       {:type  "submit"
        :value "Bli med"}]
      (anti-forgery-field)]]]
   [:div.pure-u.pure-u-md-1-5]])

(defn web-page [state & opts]
  (html5
    (html-head "Mumrik :: Din treningsdagbok og personlige trener")
    [:body
     (landing-header)
     [:div.content
      (case state
        :account-exists
        (account-exists opts)
        :account-created
        (account-created opts)
        :account-activated
        (account-activated)
        :landing
        (landing)
        :signup
        (signup)
        ; default
        [:div])]

     (when (= :landing state)
       (footer))

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100935519ns.gif"}]]]]))

#_(defn tour-page []
  (html5
    (html-head "Tour | Movement Session")
    [:body
     (top-menu)
     [:div.content
      [:div.pure-g
       [:h2.content-head.center.pure-u-1 "Create workout sessions"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "
         The next workout is a click of a button away. Continue a plan you're enrolled in or create a new inspiring session.
         You can get a session picked randomly, or you can select something you or others have created.
         If a generated session is not to your liking you can quickly replace it with another."]]]
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1.center
          [:img.pure-img-responsive {:src "images/marketing/tour-session.png"}]]]]]

      [:div.pure-g
       [:div.pure-u-1
        [:div.pure-g
         [:h2.content-head.is-center.pure-u-1 "Go from static to dynamic workouts"]]
        [:div.pure-g
         [:p.pure-u-1 "Stop searching for static and fully planned workouts.
         Use Movement Session to create beautifully presented, dynamic workouts and embrace variation, learning and new movement challenges."]]
        [:div.pure-g
         [:p.pure-u-1.center
          [:img.pure-img-responsive {:src "images/marketing/old-style-to-new.png"}]]]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Interactivity"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "You can always expand any part of a session by adding more movements of the same kind.
         You can also add any specific movement by searching for it."]]
        [:div.pure-g
         [:p.pure-u-1 "Maybe you don't have access to some equipment a movement needs, like a bar or some weights.
         The green crossing arrows allows you to swap any movement with another one."]]
        [:div.pure-g
         [:p.pure-u-1 "If a movement shows the arrow icons they can be replaced by easier and/or harder variations.
         Swapping the movement with a variation is a great way to adjust the difficulty of the session or to learn a new movement."]]
        [:div.pure-g
         [:p.pure-u-1 "Repetitions, sets, rest period, distance, duration and weights can all be set. This way you can log your efforts in detail."]]]
       [:div.pure-u.pure-u-lg-1-2
        [:p.pure-u-1.center
         [:img.pure-img-responsive {:src "images/marketing/session-circuit.png"}]]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Log & Share"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p "
        Add how long your session lasted and write a comment. Your session will be stored with all the data you provide and
        the session can be reviewed at a later time. Each session gets a unique url address so you can share your workout with your friends.
        "]]
       [:div.pure-u.pure-u-lg-1-2.center
        [:img.pure-img-responsive {:src "images/marketing/time-comment.png"}]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Create your own workouts and plans"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p.center
         [:img.pure-img-responsive {:src "images/marketing/create-plan.png"}]]]
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "
         You can create your own sessions,
         you can collect sessions in groups
         and you can create plans that span over days or weeks."]]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Explore and reuse what other users create"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "Exploring the movement database is a sure-fire way to understand how Movement Session works,
         to learn new movements and to be inspired to create your own content."]]
        [:div.pure-g
         [:p.pure-u-1 "
         You can browse through the different categories.
         By selecting the category 'Handstand' we can see all the movements in the database that are related to this category.
         You can also search for and view a specific movement.
         "]]]
       [:div.pure-u.pure-u-lg-1-2
        [:p.center
         [:img.pure-img-responsive {:src "images/marketing/explore-movements-2.png"}]]]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p.center
         [:img.pure-img-responsive {:src "images/marketing/explore-templates.png"}]]]
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1.l-m "Templates, groups and plans can be found by searching for words or by categories used."]]
        [:div.pure-g
         [:p.pure-u-1.l-m "You can always find content you have created, content movementsession has created and all the content
         that is currently linked to your account."]]]]
      [:div.pure-g
       [:p.pure-u-1.center
        [:a.m-button.orange.x-large.upper {:title  "Sign Up" :href   "/signup" :target ""} "Try Movement Session"]]]]
     (footer)

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]

     ]))

