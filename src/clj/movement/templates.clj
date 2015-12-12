(ns movement.templates
  (:require [datomic.api :as d]
            [movement.db :refer [tx]]))

(defn add-standard-templates-to-user [user]
  (let [tx-data [{:db/id #db/id[:db.part/user -1] :category/name "Strength"}
                 {:db/id #db/id[:db.part/user -2] :category/name "Mobility"}
                 {:db/id #db/id[:db.part/user -3] :category/name "Bent Arm Strength"}
                 {:db/id #db/id[:db.part/user -4] :category/name "Straight Arm Scapular Strength"}
                 {:db/id #db/id[:db.part/user -5] :category/name "Locomotion"}
                 {:db/id #db/id[:db.part/user -6] :category/name "Lower Body Strength"}
                 {:db/id #db/id[:db.part/user -8] :category/name "Core Strength"}
                 {:db/id #db/id[:db.part/user -9] :category/name "Abdominal Strength"}
                 {:db/id #db/id[:db.part/user -10] :category/name "Lower Back Strength"}
                 {:db/id #db/id[:db.part/user -11] :category/name "Bodyweight"}
                 {:db/id #db/id[:db.part/user -12] :category/name "Jumping"}
                 {:db/id #db/id[:db.part/user -13] :category/name "Crawling"}
                 {:db/id #db/id[:db.part/user -14] :category/name "Lifting"}
                 {:db/id #db/id[:db.part/user -15] :category/name "Rolling"}
                 {:db/id #db/id[:db.part/user -16] :category/name "Walking"}
                 {:db/id #db/id[:db.part/user -17] :category/name "Acrobatics"}
                 {:db/id #db/id[:db.part/user -18] :category/name "Balancing"}
                 {:db/id #db/id[:db.part/user -19] :category/name "Hanging"}
                 {:db/id #db/id[:db.part/user -20] :category/name "Climbing"}
                 {:db/id #db/id[:db.part/user -21] :category/name "Grip Strength"}
                 {:db/id #db/id[:db.part/user -22] :category/name "Multiplane"}
                 {:db/id #db/id[:db.part/user -23] :category/name "Muscle Up"}
                 {:db/id #db/id[:db.part/user -24] :category/name "Ring"}
                 {:db/id #db/id[:db.part/user -25] :category/name "Static"}
                 {:db/id #db/id[:db.part/user -26] :category/name "Shoulder Mobility"}
                 {:db/id #db/id[:db.part/user -27] :category/name "Wrist Mobility"}
                 {:db/id #db/id[:db.part/user -28] :category/name "Spine Mobility"}
                 {:db/id #db/id[:db.part/user -29] :category/name "Bridge"}
                 {:db/id #db/id[:db.part/user -30] :category/name "Equilibre"}
                 {:db/id #db/id[:db.part/user -31] :category/name "Handstand"}
                 {:db/id #db/id[:db.part/user -32] :category/name "Headstand"}
                 {:db/id #db/id[:db.part/user -33] :category/name "Air Baby"}
                 {:db/id #db/id[:db.part/user -34] :category/name "Endurance"}
                 {:db/id #db/id[:db.part/user -35] :category/name "Jump Rope"}
                 {:db/id #db/id[:db.part/user -36] :category/name "Running"}
                 {:db/id #db/id[:db.part/user -37] :category/name "Weights"}
                 {:db/id #db/id[:db.part/user -38] :category/name "Lunge"}
                 {:db/id #db/id[:db.part/user -39] :category/name "Lifting"}
                 {:db/id #db/id[:db.part/user -40] :category/name "Pushing"}
                 {:db/id #db/id[:db.part/user -41] :category/name "Leg Strength"}
                 {:db/id #db/id[:db.part/user -42] :category/name "Single Leg Squat"}
                 {:db/id #db/id[:db.part/user -43] :category/name "Ankle Mobility"}
                 {:db/id #db/id[:db.part/user -44] :category/name "Hip Mobility"}
                 {:db/id #db/id[:db.part/user -45] :category/name "Pulling"}
                 {:db/id #db/id[:db.part/user -46] :category/name "Pull Up"}

                 {:db/id #db/id[:db.part/user -48] :category/name "Push Up"}
                 {:db/id #db/id[:db.part/user -49] :category/name "Dip"}
                 {:db/id #db/id[:db.part/user -50] :category/name "Back Lever"}
                 {:db/id #db/id[:db.part/user -51] :category/name "Front Lever"}
                 {:db/id #db/id[:db.part/user -52] :category/name "Side Lever"}
                 {:db/id #db/id[:db.part/user -53] :category/name "Planche"}
                 {:db/id #db/id[:db.part/user -54] :category/name "Throwing"}
                 {:db/id #db/id[:db.part/user -55] :category/name "Walking"}
                 {:db/id #db/id[:db.part/user -56] :category/name "Carrying"}

                 {:db/id #db/id[:db.part/user -5000] :movement/unique-name "Run"}
                 {:db/id #db/id[:db.part/user -5001] :movement/unique-name "Back Squat"}
                 {:db/id #db/id[:db.part/user -5002] :movement/unique-name "Wrist Stretches"}
                 {:db/id #db/id[:db.part/user -5003] :movement/unique-name "Dynamic Reach"}

                 {:db/id         #db/id[:db.part/user -999]
                  :user/email    user
                  :user/template [#db/id[:db.part/user -101]
                                  ;#db/id[:db.part/user -102]
                                  #db/id[:db.part/user -103]
                                  ;#db/id[:db.part/user -104]
                                  ;#db/id[:db.part/user -105]
                                  #db/id[:db.part/user -106]
                                  ;#db/id[:db.part/user -107]
                                  #db/id[:db.part/user -109]
                                  ]}

                 {:db/id          #db/id[:db.part/user -101]
                  :template/title "Locomotion"
                  :template/description
                                  "Let's practice different crawls, walks and rolls as ways of moving across the floor.
                                  Warm up well with some varied mobility work.
                                  Then go through the locomotion movements for between 4 and 6 rounds.
                                  Try to keep a smooth, controlled and even tempo.
                                  Use minimal rest between each movement."
                  :template/part  [#db/id[:db.part/user -1011]
                                   #db/id[:db.part/user -1012]]}
                 {:db/id                    #db/id[:db.part/user -1011]
                  :part/title               "Mobility"
                  :part/category            [#db/id[:db.part/user -2]]
                  :part/specific-movement  [#db/id[:db.part/user -5002]
                                            #db/id[:db.part/user -5003]]
                  :part/number-of-movements 4
                  :part/rep                 10
                  :part/set                 1}
                 {:db/id                    #db/id[:db.part/user -1012]
                  :part/title               "Locomotion"
                  :part/category            [#db/id[:db.part/user -13]
                                             #db/id[:db.part/user -15]
                                             #db/id[:db.part/user -16]]
                  :part/number-of-movements 6
                  :part/distance            15
                  :part/set                 4}

                 #_{:db/id          #db/id[:db.part/user -102]
                    :template/title "Calisthenics"
                    :template/description
                                    "Bodyweight movements!"
                    :template/part  [#db/id[:db.part/user]]}


                 {:db/id          #db/id[:db.part/user -103]
                  :template/title "4x4 Interval Run"
                  :template/description
                                  "Interval work is excellent endurance training and increases aerobic capacity.
                                  Begin with a 10 minute warmup to increase your body temperature and mobilize your legs.
                                  Do four interval runs, with three minutes of active rest between intervals."
                  :template/part  [#db/id[:db.part/user -1031]
                                   #db/id[:db.part/user -1032]]}
                 {:db/id                    #db/id[:db.part/user -1031]
                  :part/title               "10 min warmup"
                  :part/category            [#db/id[:db.part/user -43]
                                             #db/id[:db.part/user -44]]
                    :part/specific-movement       [#db/id[:db.part/user -5000]]
                  :part/number-of-movements 3}
                 {:db/id              #db/id[:db.part/user -1032]
                  :part/title         "4x4"
                  ;:part/category      []
                  :part/specific-movement [#db/id[:db.part/user -5000]]
                  :part/duration      240
                  :part/set           4}


                 #_{:db/id          #db/id[:db.part/user -104]
                    :template/title "Learning Handstand"
                    :template/description
                                    ""
                    :template/part  [#db/id[:db.part/user]]}


                 #_{:db/id          #db/id[:db.part/user -105]
                    :template/title "Play On Rings"
                    :template/description
                                    ""
                    :template/part  [#db/id[:db.part/user]]}

                 {:db/id          #db/id[:db.part/user -106]
                    :template/title "Lifting Weights 5x5"
                    :template/description
                                    "Get stronger by lifting some weights!
                                    Make it a goal to add weight with each session.
                                    The first of these sessions should be performed using light weights.
                                    Increase the weights by 2-3kg/4-5lbs with each new session, for as long as possible.
                                    \n
                                    After the warmup, perform a couple of lifts with easy weights,
                                    focusing on perfecting the lifting technique.
                                    Then do backsquats for five reps and five sets.
                                    Finally perform two more lifts for five reps and five sets.
                                    Remember to add which weights was used in the comment section."
                    :template/part  [#db/id[:db.part/user -1061]
                                     #db/id[:db.part/user -1062]
                                     #db/id[:db.part/user -1063]
                                     #db/id[:db.part/user -1064]]}
                 {:db/id                    #db/id[:db.part/user -1061]
                  :part/title               "General warmup"
                  :part/category            [#db/id[:db.part/user -2]]
                  :part/number-of-movements 4
                  :part/rep                 10
                  :part/set                 3}
                 {:db/id                    #db/id[:db.part/user -1062]
                  :part/title               "Technical Work"
                  :part/category            [#db/id[:db.part/user -14]]
                  :part/number-of-movements 2
                  :part/rep                 10
                  :part/set                 3}
                 {:db/id                    #db/id[:db.part/user -1063]
                  :part/title               "Squat"
                  :part/category            [#db/id[:db.part/user -14]]
                  :part/specific-movement   [#db/id[:db.part/user -5001]]
                  :part/rep                 5
                  :part/set                 5}
                 {:db/id                    #db/id[:db.part/user -1064]
                  :part/title               "Lifting"
                  :part/category            [#db/id[:db.part/user -14]]
                  :part/number-of-movements 2
                  :part/rep                 5
                  :part/set                 5}


                 #_{:db/id          #db/id[:db.part/user -107]
                    :template/title "Climbing"
                    :template/description
                                    ""
                    :template/part  [#db/id[:db.part/user]]}

                 {:db/id          #db/id[:db.part/user -109]
                  :template/title "Natural Movement"
                  :template/description
                                  "Practice movement skills, such as crawling, jumping, climbing and lifting.
                                  Go through the warmup and combination parts doing 10 reps of each movement before moving on to the next one.
                                  Complete all rounds without rest, focusing on quality over speed.
                                  If possible, do manipulation movements with non-uniform objects like rocks or logs."
                  :template/part  [#db/id[:db.part/user -1091]
                                   #db/id[:db.part/user -1092]
                                   #db/id[:db.part/user -1093]]}
                 {:db/id                    #db/id[:db.part/user -1091]
                  :part/title               "Warmup/Mobility (3 rounds)"
                  :part/category            [#db/id[:db.part/user -43]
                                             #db/id[:db.part/user -44]]
                  :part/number-of-movements 4
                  :part/rep                 10
                  :part/set                 3}
                 {:db/id                    #db/id[:db.part/user -1092]
                  :part/title               "Skill"
                  :part/category            [#db/id[:db.part/user -12]
                                             #db/id[:db.part/user -13]
                                             #db/id[:db.part/user -14]
                                             #db/id[:db.part/user -15]
                                             #db/id[:db.part/user -16]
                                             #db/id[:db.part/user -18]
                                             #db/id[:db.part/user -19]
                                             #db/id[:db.part/user -20]
                                             #db/id[:db.part/user -36]
                                             #db/id[:db.part/user -54]
                                             #db/id[:db.part/user -55]
                                             #db/id[:db.part/user -56]]
                  :part/number-of-movements 1
                  :part/rep                 30
                  :part/set                 1}
                 {:db/id                    #db/id[:db.part/user -1093]
                  :part/title               "Combination (3-5 rounds)"
                  :part/category            [#db/id[:db.part/user -12]
                                             #db/id[:db.part/user -13]
                                             #db/id[:db.part/user -14]
                                             #db/id[:db.part/user -15]
                                             #db/id[:db.part/user -16]
                                             #db/id[:db.part/user -18]
                                             #db/id[:db.part/user -19]
                                             #db/id[:db.part/user -20]
                                             #db/id[:db.part/user -36]
                                             #db/id[:db.part/user -54]
                                             #db/id[:db.part/user -55]
                                             #db/id[:db.part/user -56]]
                  :part/number-of-movements 5
                  :part/rep                 10
                  :part/set                 3}

                 ]]
    (d/transact (:conn @tx) tx-data)))