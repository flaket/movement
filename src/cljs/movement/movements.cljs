(ns movement.movements)

;; -------------------------
;; Movements in categories
; movnat
(def movnat-sitting [:split-stand-to-split-sit :walking-split-squat :side-shuffle
                     :stepping-under :stepping-over :hinged-walk :split-squat
                     :squat-to-medium-kneeling :squat-to-kneeling :kneeling-to-tall-kneeling
                     :kneeling-to-tall-half-kneeling :kneeling-to-lateral-half-kneeling
                     :split-knee-walk :tall-half-kneeling-to-tall-split-kneeling
                     :deep-squat :deep-knee-bend :deep-squat-to-deep-knee-bend
                     :deep-squat-narrow-base :tall-half-kneeling-to-kneeling
                     :tall-split-kneeling-to-bent-sit :deep-knee-bend-to-kneeling
                     :stepping-up :backward-stepping-down :extended-stepping-up
                     :forward-stepping-down])
(def movnat-balancing [:tip-toe-balancing :balancing-walk :balancing-split-squat
                       :balancing-shuffle :cross-reverse :pivot-reverse
                       :split-squat-pivot-reverse :balancing-tripod-transition
                       :balancing-foot-hand-crawl])
(def movnat-crawling [:inverted-crawl :knee-elbow-crawl :knee-hand-crawl :foot-elbow-crawl
                      :foot-hand-crawl :push-pull-crawl :rocking :rotational-rocking
                      :bent-sit-to-lateral-half-kneeling :shoulder-crawl :hip-thrust-crawl
                      :sit-to-backward-roll :lying-to-forward-roll :forward-roll :tripod-transition
                      :backward-sprawl :forward-sprawl-to-push-up])
(def movnat-jumping [:leg-swing-jump :broad-jump :split-jump :vertical-jump :depth-jump :lateral-side-jump])
(def movnat-climbing [:tuck-swing :tap-swing :side-swing :side-swing-traverse :side-swing-hang-hold
                      :side-swing-power-traverse :pull-up :pull-up-reach :pull-up-hold :one-arm-dead-hang
                      :jumping-pull-up :hanging-side-foot-lift :hanging-leg-hook-over
                      :hanging-knee-tuck :hanging-knee-to-bar :hanging-front-foot-lift
                      :front-swing-traverse :front-power-traverse :elbow-pull-up :dead-hang
                      :sliding-swing-up :elbow-pop-up :hand-swing :swing-pop-up :tuck-pop-up
                      :muscle-up :roll-up])
(def movnat-lifting [:dead-lift :lapping :waist-carry :chest-carry :hand-carry :shoulder-carry
                     :log-shouldering :shoulder-carry-squat :hands-free-shoulder-carry-squat
                     :shoulder-carry-switch :clean :jerk :push-press])
(def movnat-throwing [:chest-throw :rotational-throw :front-swing-throw :overhead-throw])
(def movnat-running [:run-100-m :run-200-m :run-400-m :sprint-50-m])

; Warmup
(def warmup [:joint-mobility :jump-rope :running])

; Mobility
(def hip-mobility (concat movnat-sitting [:dynamic-squat-stretch :floor-hip-rotations :squat-hip-rotations
                                          :toes-stretch :dynamic-toes-stretch :static-squat
                                          :dynamic-squats :knee-push-aways :sky-reaches :buddha-prayers
                                          :squat-bows]))
(def shoulder-mobility [:external-rotation :lateral-raise :hands-overhead-band-rotations-and-flyes
                        :hands-side-band-rotations-and-flyes :hands-down-band-rotations-and-flyes
                        :scapula-push-up :overhead-straight-arm-pulldown :whippet :cuban-rotations
                        :shoulder-dislocations])
(def wrist-mobility [:wrist-prep-routine])
(def ankle-mobility [:ankle-prep-routine])
(def spine-mobility [:slide-into-low-bridge :rotate-into-low-bridge
                     :wall-rotations :rotate-into-high-bridge :high-bridge-hold
                     :jefferson-curl])

; Hanging
(def hanging [:passive-hang :active-hang :false-grip-hang :side-to-side-swing
              :arching-active-hang :front-stationary-swing :one-arm-passive :one-arm-active
              :swing-grip-routine :figure-8])
; Locomotion
(def locomotion [:kick-to-handstand :cart-wheel :handstand-walk :bridge-walk
                 :duck-walk :horse-walk :lizard-crawl :ostrich-walk :bear-walk
                 :crab-walk :inchworm])
; Equilibre
(def equilibre [:headstand-leg-lift :wall-walk :wall-kick :handstand-walk
                :handstand-push-up :air-baby :qdr])
; Leg strength
(def leg-strength [:basic-squat :back-squat :front-squat :overhead-squat
                   :basic-lunge :back-lunge :front-lunge :overhead-lunge
                   :deadlift :pistol :shrimp :behind-leg-squat
                   :jump-onto-box-from-standing :jump-onto-box-from-squatting
                   :front-flip :back-flip :natural-leg-curl])
; Auxiliary strength
(def auxiliary [:l-sit :l-straddle :v-up :sitting-leg-lift :swedish-leg-lift
                :hanging-leg-lift :headstand-leg-lift :archup])
; Straight arm scapular strength
(def sass [:swedish-bar-front-support :swedish-bar-hold-back
           :back-lever :front-lever :side-lever :planche :handstand-work
           :active-hanging-work])
; Bent arm strength
(def bas [:push-up-basic :push-up-russian :push-up-wide
          :push-up-diamond :push-up-hindu :push-up-lateral :push-up-bridge
          :push-up-archer :push-up-one-arm :push-up-one-leg-one-arm
          :dip-basic :dip-russian :dip-single-bar :dip-korean :dip-ring
          :dip-ring-wide :dip-ring-archer
          :handstand-push-up-head-wall :handstand-push-up-wall :handstand-push-up-free
          :push-up-planche :pull-up-basic :pull-up-wide :pull-up-rings :pull-up-rings-wide
          :pull-up-chest :pull-up-waist :pull-up-weighted :pull-up-scapula
          :one-arm-pull-up-forearm :one-arm-pull-up-bicep :one-arm-negative
          :archer-pull-up :one-arm-pull-up-shoulder
          :one-arm-pull-up :row-basic :row-wide :front-lever-row
          :german-hang-pull :pull-over :front-lever-pull :back-lever-pull
          :tick-tock :back-lever-negative :front-lever-negative
          :muscle-up :pull-up-false-grip :muscle-up-negative
          :muscle-up-l-sit :rope-climb])

; Maya
(def m-styrke [:push-up :lunge :knebøy :knebøy-med-vekt :markløft :vekt-press-over-hodet
               :balanse-trepunkts-overgang :hodestående])
(def m-oppvarming [:hånd-fot-kravle-20m :invertert-kravle-20m :ta-i-gulvet-med-strake-bein-20-reps :strikk-hengsle-armene
                   :strikk-dra-fra-hverandre :rotere-hode-skuldre-hofter-og-ankler
                   :stående-til-sittende :gående-lunges :gå-over :gå-under
                   :knebøy-til-knesittende :høy-bro-5-reps :lav-bro-5-reps :jefferson-curl :høy-bro-hold-30s])
(def m-kombinasjon (concat m-styrke m-oppvarming))


;----

(def mobility (concat hip-mobility shoulder-mobility
                      wrist-mobility ankle-mobility spine-mobility))
(def strength (concat leg-strength auxiliary sass bas))
(def movnat (concat movnat-sitting movnat-balancing movnat-crawling movnat-jumping movnat-climbing
                    movnat-lifting movnat-throwing movnat-running))
(def movnat-warmup (concat movnat-sitting movnat-balancing movnat-crawling))

(def all-categories {:hip-mobility      hip-mobility
                     :shoulder-mobility shoulder-mobility
                     :wrist-mobility    wrist-mobility
                     :ankle-mobility ankle-mobility})
(def all-movements (concat warmup mobility strength movnat))

(defonce morning-ritual-template
         {:title "Morning Ritual"
          :parts [{:title "Warmup" :category warmup :n 1}
                  {:title "Mobility" :category mobility :n 5}
                  {:title "Hanging" :category hanging :n 1}
                  {:title "Equilibre" :category equilibre :n 1}
                  {:title "Strength" :category strength :n 1}]})
(defonce strength-template
         {:title "Strength"
          :parts [{:title "Warmup" :category warmup :n 1}
                  {:title "Mobility" :category mobility :n 6}
                  {:title "Strength" :category strength :n 4}]})
(defonce mobility-template {:title "Mobility/Prehab"
                            :parts [{:title "Warmup" :category warmup :n 1}
                                    {:title "Mobility" :category mobility :n 6}
                                    {:title "Prehab" :category mobility :n 4}]})
(defonce locomotion-template {:title "Locomotion"
                              :parts [{:title "Warmup" :category warmup :n 1}
                                      {:title "Mobility" :category mobility :n 6}
                                      {:title "Locomotion (4-6 rounds)"
                                       :category locomotion :n 6}]})
(defonce bas-template {:title "Bent Arm Strength"
                       :parts [{:title "Warmup" :category warmup :n 1}
                               {:title "Mobility" :category mobility :n 6}
                               {:title "BAS" :category bas :n 4}]})
(defonce sass-template {:title "Straight Arm Scapular Strength"
                        :parts [{:title "Warmup" :category warmup :n 1}
                                {:title "Mobility" :category mobility :n 6}
                                {:title "SASS" :category sass :n 4}]})
(defonce leg-strength-template {:title "Leg Strength"
                                :parts [{:title "Warmup" :category warmup :n 1}
                                        {:title "Mobility" :category mobility :n 4}
                                        {:title "Leg Strength" :category leg-strength :n 3}
                                        {:title "Auxiliary" :category auxiliary :n 2}]})
(defonce movnat-template {:title "MovNat"
                          :parts [{:title "Warmup Mobility (3 rounds)" :category movnat-warmup :n 4}
                                  {:title "Skill (30 reps)" :category movnat :n 1}
                                  {:title "Combo (4 rounds)" :category movnat :n 5}]})
(defonce maya-template {:title "Maya"
                        :parts [{:title "Oppvarming/Bevegelighet (2 runder rolig)"
                                 :category m-oppvarming :n 3}
                                {:title "Styrke/Ferdighet (30 reps)" :category m-styrke :n 1}
                                {:title "Kombinasjon (3 runder hurtig)" :category m-kombinasjon
                                 :n 4}]})

(def all-templates (conj [] morning-ritual-template strength-template mobility-template
                           locomotion-template bas-template sass-template leg-strength-template
                           movnat-template maya-template))