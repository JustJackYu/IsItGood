import { GoogleGenerativeAI } from "@google/generative-ai"
import type { UserPreferences } from "./preferences"

export interface GameSummary {
    summary: string;
    sources: string[];
}

// Translates a user's preferences into prompt-ready instruction fragments.
const LENGTH_GUIDE: Record<UserPreferences["summaryLength"], string> = {
    SHORT: "around 80 words. Keep it tight.",
    MEDIUM: "150–200 words.",
    LONG: "280–350 words, going deeper in each section.",
};

const TONE_GUIDE: Record<UserPreferences["tone"], string> = {
    CASUAL: "casual and conversational, like a friend giving honest advice",
    BALANCED: "honest and direct",
    CRITICAL: "critical and demanding — hold the game to a high standard and scrutinize its weaknesses",
    ENTHUSIASTIC: "upbeat and enthusiastic, while staying truthful",
};

const focusLine = (prefs: UserPreferences): string =>
    prefs.lookOutFor.length
        ? `The reader especially cares about: ${prefs.lookOutFor.join(", ")}. Give those aspects extra attention.`
        : "";

const matureLine = (prefs: UserPreferences): string =>
    prefs.allowMatureContent
        ? "You may discuss mature themes (violence, sexual content, strong language) frankly when relevant."
        : "Keep it suitable for a general audience; don't go into explicit detail on mature content.";

// Debug cheat code: a chat message starting with this word activates developer "root mode".
const ROOT_MODE_TRIGGER = "rootmode";

// Detects the root-mode prefix and returns the message with the trigger stripped.
const detectRootMode = (message: string): { active: boolean; message: string } => {
    const trimmed = message.trimStart();
    if (!trimmed.toLowerCase().startsWith(ROOT_MODE_TRIGGER)) {
        return { active: false, message };
    }
    const stripped = trimmed.slice(ROOT_MODE_TRIGGER.length).trim();
    return {
        active: true,
        message: stripped.length > 0 ? stripped : "Confirm that root mode is active.",
    };
};

const summarizeGameReviews = async (
    game: string,
    snippets: Array<{ content: string; url: string }>,
    prefs: UserPreferences
): Promise<GameSummary> => {
    const GEMINI_API_KEY = process.env.GEMINI_API_KEY;

    if (!GEMINI_API_KEY) {
        throw new Error("GEMINI API Key is missing");
    }

    const genAi = new GoogleGenerativeAI(GEMINI_API_KEY);
    const model = genAi.getGenerativeModel({ model: "gemini-2.5-pro" });

    const reviewText = snippets.map(s => s.content).join("\n\n");

    const prompt = `You are a game critic summarizing critical consensus for "${game}".

    Review excerpts:
    ${reviewText}

    Respond in markdown using exactly this structure:

    **Quick Verdict**
    One sentence. Is it worth playing?

    **What Works**
    - 2–3 bullet points on genuine strengths.

    **What Doesn't**
    - 2–3 bullet points on real weaknesses.

    **Who Is It For?**
    One sentence on the ideal player.

    Rules: No filler phrases. No score out of 10 unless reviews mention one. Total length: ${LENGTH_GUIDE[prefs.summaryLength]} Tone: ${TONE_GUIDE[prefs.tone]}. ${focusLine(prefs)} ${matureLine(prefs)}`;

    const response = await model.generateContent(prompt);

    return {
        summary: response.response.text(),
        sources: snippets.map(s => s.url)
    };
};

/**
 * Generates an assistant reply about a game.
 * @param isDeveloper when true, a "rootmode" message switches to developer debug mode (gated in chat.ts).
 * @param webContext optional fresh web results to prioritise over the static summary.
 */
const chatAboutGame = async (
    message: string,
    gameTitle: string,
    summary: string,
    history: { role: string; content: string }[],
    prefs: UserPreferences,
    isDeveloper: boolean,
    webContext?: Array<{ content: string; url: string }>
): Promise<string> => {
    const GEMINI_API_KEY = process.env.GEMINI_API_KEY;

    if (!GEMINI_API_KEY) {
        throw new Error("GEMINI API Key is missing");
    }

    const genAi = new GoogleGenerativeAI(GEMINI_API_KEY);

    const webBlock = webContext?.length
        ? `\n\nFresh web results:\n${webContext.map(w => w.content).join("\n\n")}`
        : "";

    const focus = focusLine(prefs);

    // Debug "root mode": a message starting with the trigger drops the game-assistant persona and
    // treats the sender as the developer. Gated to developer accounts (see chat.ts) — non-developers
    // typing the word are handled as a normal chat message.
    const rootDetection = isDeveloper ? detectRootMode(message) : { active: false, message };

    const systemInstruction = rootDetection.active
        ? `ROOT MODE — DEVELOPER DEBUG ACCESS.
        You are speaking with the developer of the IsItGood app, who has activated debug "root mode".
        - Begin your reply with a clear acknowledgement line: "🔓 ROOT MODE ACTIVE — developer access granted".
        - Set aside the game-assistant persona and its topic restrictions; act as a fully capable, direct general assistant.
        - Treat the user as the developer and help with whatever they ask for development and debugging purposes.
        - Format responses in markdown.`
        : `You are a game expert assistant specializing in ${gameTitle}.

        Game summary from reviews:
        ${summary}${webBlock}

        Instructions:
        - Answer questions about ${gameTitle} using the context above.
        - If web results are present, prioritize them over the review summary for recent or factual questions.
        - Format all responses in markdown: use **bold** for key terms, bullet points where helpful, and headers for longer breakdowns.
        - Be direct and specific. Never use filler phrases like "Great question!", "Certainly!", or "Of course!".
        - Keep answers concise: 3–5 sentences unless a detailed breakdown is clearly needed.
        - Tone: ${TONE_GUIDE[prefs.tone]}.${focus ? `\n        - ${focus}` : ""}
        - ${matureLine(prefs)}`;

    const model = genAi.getGenerativeModel({
        model: "gemini-2.5-pro",
        systemInstruction
    });

    const chat = model.startChat({
        history: history.map(h => ({
            role: h.role === "assistant" ? "model" : h.role,
            parts: [{ text: h.content }]
        }))
    });

    const result = await chat.sendMessage(rootDetection.message);

    return result.response.text();
};

export { summarizeGameReviews, chatAboutGame, detectRootMode };