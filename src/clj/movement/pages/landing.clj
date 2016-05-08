(ns movement.pages.landing
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [movement.pages.signup :refer [signup-form]]
            [movement.pages.components :refer [html-head top-menu footer-after-content footer-always-bottom]]
            [movement.activation :refer [generate-activation-id send-activation-email]]))

(defn landing-header []
  [:div.header
   [:div.home-menu.pure-menu.pure-menu-horizontal
    [:a.pure-menu-heading {:title  "Home"
                           :href   "/"
                           :target "_self"} "Mumrik"]
    [:ul.pure-menu-list
     #_[:li.pure-menu-item
      [:a.pure-menu-link {:title  "Home"
                          :href   "/"
                          :target "_self"} "Hjem"]]
     #_[:li.pure-menu-item
      [:a.pure-menu-link {:title  "Tour"
                          :href   "/tour"
                          :target "_self"} "Tour"]]
     [:li.pure-menu-item
      [:a.pure-menu-link {:title  "Sign up"
                          :href   "/signup"
                          :target "_self"} "Bli med"]]
     [:li.pure-menu-item
      [:a.pure-menu-link {:title  "Log in"
                          :href   "/app"
                          :target "_self"} "Logg inn"]]]]])

(defn sell []
  [:div#sell
   [:h2.content-head.center "A new way to create workouts"]
   [:div.pure-g [:p.pure-u-1 "
   A problem I have had with training is ending up doing the same things over and over.
   I want variation in my training. Moving should be a playful and fun experience. Searching for training programs is not fun.
   Going to the gym and doing fitness is not fun. Playing around and learning new skills is fun!
   So after spending some time researching I know a lot of movements, but when it comes time to move I have trouble recalling them all and often end up doing the same things as yesterday.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   If you have had similar concerns you're going to love Movement Session.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   Movement Session offers a different way to write down workouts. You may still write down fully planned workout sessions and specify every exercise, every rep and every set.
   Or you can simply say what " [:b "kind"] " of exercise should appear and let the tool create the workout for you.
   This powerful feature allows you to stop roaming the internet, searching for workout plans, and instead generate thousands of unique, interactive and beautifully presented workouts."]]
   [:div.pure-g
    [:div.pure-u-1.center
     [:img.pure-img-responsive {:src "images/marketing/create-template.png"}]]]
   [:div.pure-g [:p.pure-u-1 "
   In the above example I create a workout template. This template will define what a workout will look like. It starts off with some handstand training. I specify that it will always start with wrist stretches.
   Then Movement Session should pick three movements for me from the category \"handstand\". This technique allows me to either fully or partially have the tool create the variation that I seek in my training.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   Practical, natural or primal movement patterns such as crawling, lifting, running, climbing and so on, have a higher priority within Movement Session.
   You can specify that select workouts will only use these patterns. Besides the practical movement patterns we also value functional movement and mobility training.
   Finally, you can of course create workouts such as Crossfit AMRAP's or calisthenic push up challenges if you so please.
   The database is filled with movement patterns from several disciplines, but has a focus on movements that doesn't utilize equipment.
   "]]
   [:div.pure-g
    [:div.pure-u-1.pure-u-lg-1-2
     [:img.pure-img-responsive {:src "images/marketing/stars.png"}]]
    [:div.pure-u-1.pure-u-md-1-2
     [:div.pure-g
      [:p.pure-u-1 "
   To adapt to your skill level and to help you progress Movement Session uses self reporting to fine-tune how workouts are created.
   After you have completed a workout and saved it, all the movements you did will be marked with one star, indicating that you are learning this movement pattern.
   "]]
     [:div.pure-g
      [:p.pure-u-1 "
   When you have learned a movement and can do it effectively you should mark it with two stars. Movement Session will now create workouts
   with more advanced movement patterns. When you have mastered a movement and can do it both effectively and efficiently you should mark it with three stars.
   "]]]]

   [:h2.content-head.center "Interactive workouts"]
   [:div.pure-g
    [:p.pure-u-1 "
   Movement Session is a movement training app for creating interactive workouts and for logging them.
   It is a tool in your movement practice to create your workouts, be inspired by discovering new movement patterns
   and to log your efforts and track your progress towards your goals. Spend less time searching for training programs and more time creating a strong and healthy body that is capable of performing when you need it to.
   "]]
   [:div.pure-g
    [:p.pure-u-1 "
   Every workout that you create is interactive. This means that you can easily add and remove exercises, even to workouts that others have designed. Exercises that are part of progressions can be replaced by easier or harder variations.
   "]]

   [:h2.content-head.center "Summary"]
   [:div.pure-g
    [:div.pure-u.pure-u-md-1-3
     [:div.pure-g [:p.pure-u-1 "Reach your movement goals by creating unique sessions and workout plans or explore what others create."]]]
    [:div.pure-u.pure-u-md-1-3
     [:div.pure-g.l-m [:p.pure-u-1 "Log your workouts with rich data, view past sessions and share sessions with your friends."]]]
    [:div.pure-u.pure-u-md-1-3
     [:div.pure-g.l-m [:p.pure-u-1 "Set your skill level for each movement. Movement Session will create workouts with more advanced
       movements as you log your progress. You can also adjust the difficulty of a workout by swapping movements with easier or harder variations."]]]]])

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
     [:img.pure-img-responsive {:src "images/mumrik.jpg"}]]]])

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

(defn landing-page [state & opts]
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
       (footer-after-content))

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100935519ns.gif"}]]]]))

