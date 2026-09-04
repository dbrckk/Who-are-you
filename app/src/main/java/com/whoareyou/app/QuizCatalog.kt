package com.whoareyou.app

data class Answer(val text: String, val score: Int)
data class Question(val text: String, val answers: List<Answer>)
data class Quiz(
    val id: String,
    val title: String,
    val hook: String,
    val time: String,
    val accent: String,
    val lowTitle: String,
    val midTitle: String,
    val highTitle: String,
    val lowDescription: String,
    val midDescription: String,
    val highDescription: String,
    val metricLow: String,
    val metricHigh: String,
    val questions: List<Question>
)

private fun q(text: String, a: String, b: String, c: String, d: String) =
    Question(text, listOf(Answer(a, 0), Answer(b, 1), Answer(c, 2), Answer(d, 3)))

val quizzes = listOf(
    Quiz(
        "social_battery", "Social Battery", "How much people can you actually handle?", "45 SEC", "◉",
        "The Quiet Core", "The Selective Social", "The Human Charger",
        "Your energy tends to recover in quiet spaces. You can enjoy people, but too much social input drains you quickly.",
        "You enjoy connection when the context feels right. Your social energy depends heavily on the people, place and mood.",
        "Interaction often gives you momentum. You tend to recharge through people, activity and shared experiences.",
        "SOLITUDE", "SOCIAL ENERGY",
        listOf(
            q("After a full day around people, what do you want most?", "Total silence", "One close person", "A relaxed group", "More plans"),
            q("A free Saturday appears. Your first instinct?", "Stay home alone", "Keep it low-key", "Meet a few people", "Fill the day with plans"),
            q("At a party where you know almost nobody...", "I look for an exit", "I stay near one person", "I warm up eventually", "I start conversations"),
            q("How do group chats usually feel?", "Exhausting", "Easy to ignore", "Fine in moderation", "I keep them alive"),
            q("After cancelling social plans, you usually feel...", "Relieved", "Mostly fine", "A little disappointed", "Like I missed out")
        )
    ),
    Quiz(
        "logic_emotion", "Logic vs Emotion", "What really drives your decisions?", "50 SEC", "◇",
        "The Rational Mind", "The Integrator", "The Intuitive Heart",
        "You tend to trust evidence, consistency and structure before feelings. Emotions matter, but they rarely get the final vote.",
        "You naturally combine analysis and feeling. You can switch between evidence and intuition depending on what is at stake.",
        "Your internal sense of what feels right strongly shapes your choices. Human impact often matters more than perfect logic.",
        "LOGIC", "EMOTION",
        listOf(
            q("Two choices are equally practical. What breaks the tie?", "The numbers", "Long-term logic", "My instinct", "What feels right"),
            q("A friend makes an irrational decision. You first...", "Point out the flaw", "Ask for the reasoning", "Try to understand", "Focus on how they feel"),
            q("When buying something expensive, you trust...", "Comparison data", "Research plus instinct", "My overall impression", "The feeling it gives me"),
            q("In an argument, what bothers you most?", "Bad logic", "Contradictions", "Being misunderstood", "Emotional coldness"),
            q("When a plan fails unexpectedly...", "Diagnose the cause", "Recalculate", "Follow my gut", "Check how everyone feels")
        )
    ),
    Quiz(
        "overthinker", "Overthinker", "Does your brain ever actually switch off?", "40 SEC", "∞",
        "The Clear Decider", "The Analyzer", "The Infinite Loop",
        "You usually process what matters and move forward. Uncertainty may bother you, but it rarely keeps your mind trapped for long.",
        "You think deeply and often replay important situations. Analysis helps you, though it can occasionally become mental noise.",
        "Your mind generates branches, alternatives and second-order consequences almost automatically. Switching off can be harder than deciding.",
        "LET GO", "OVERTHINK",
        listOf(
            q("After sending an important message, you...", "Forget about it", "Check once", "Reread it", "Analyze every possible interpretation"),
            q("Before a simple decision, how many scenarios appear?", "One", "A couple", "Several", "Basically a decision tree"),
            q("An awkward moment from years ago appears in your mind...", "Almost never", "Rarely", "Sometimes", "Far too easily"),
            q("Someone replies with just 'ok'. You think...", "Nothing", "They're busy", "Maybe something is off", "What exactly did that 'ok' mean?"),
            q("At night, your brain is usually...", "Quiet", "Slowing down", "Reviewing the day", "Running twelve tabs at once")
        )
    ),
    Quiz(
        "chaos_control", "Chaos vs Control", "Planner, improviser, or beautifully unpredictable?", "45 SEC", "✦",
        "The Architect", "The Adaptive Planner", "The Chaos Surfer",
        "Structure gives you freedom. You prefer knowing what comes next and reducing avoidable surprises before they happen.",
        "You like having a framework without becoming trapped by it. A plan is useful, but you can abandon it when reality changes.",
        "You are comfortable moving without a complete map. Improvisation, novelty and last-minute decisions can feel more alive than rigid plans.",
        "CONTROL", "CHAOS",
        listOf(
            q("A trip is next week. Your itinerary is...", "Already detailed", "Mostly planned", "A few anchors", "What itinerary?"),
            q("Your workspace usually looks...", "Precisely organized", "Mostly tidy", "Functional chaos", "Like a side quest exploded"),
            q("When plans suddenly change...", "I hate it", "I need a moment", "I adapt", "That makes it interesting"),
            q("Deadlines make you...", "Finish early", "Stay on schedule", "Sprint near the end", "Become incredibly powerful at 2 AM"),
            q("Choose a weekend style.", "Scheduled", "Planned loosely", "Decide that morning", "Follow whatever happens")
        )
    ),
    Quiz(
        "risk_taker", "Risk Taker", "How far outside certainty will you go?", "45 SEC", "△",
        "The Strategist", "The Calculated Risk", "The Edge Seeker",
        "You prefer asymmetric bets: protect the downside first, then move. Security and predictability carry real value for you.",
        "You will take meaningful risks when the upside is justified. You are neither reckless nor automatically conservative.",
        "Uncertainty can feel energizing rather than threatening. When something matters, you are often willing to move before certainty arrives.",
        "SECURITY", "RISK",
        listOf(
            q("A new opportunity has a big upside but no guarantee. You...", "Pass", "Research for a long time", "Take a measured shot", "Jump in"),
            q("Trying something with a real chance of public failure feels...", "Not worth it", "Uncomfortable", "Acceptable", "Exciting"),
            q("With spare money, you prefer...", "Protect it", "Mostly safe options", "A balanced mix", "High-upside bets"),
            q("You have 70% of the information needed. Do you act?", "No", "Usually wait", "Often yes", "Definitely"),
            q("Which sounds more painful?", "Losing what I have", "Making a bad call", "Missing a chance", "Never finding out")
        )
    )
)
