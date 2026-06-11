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

const chatAboutGame = async (
    message: string,
    gameTitle: string,
    summary: string,
    history: { role: string; content: string }[],
    prefs: UserPreferences,
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

    const model = genAi.getGenerativeModel({
        model: "gemini-2.5-pro",
        systemInstruction: `You are a game expert assistant specializing in ${gameTitle}.

        Game summary from reviews:
        ${summary}${webBlock}

        Instructions:
        - Answer questions about ${gameTitle} using the context above.
        - If web results are present, prioritize them over the review summary for recent or factual questions.
        - Format all responses in markdown: use **bold** for key terms, bullet points where helpful, and headers for longer breakdowns.
        - Be direct and specific. Never use filler phrases like "Great question!", "Certainly!", or "Of course!".
        - Keep answers concise: 3–5 sentences unless a detailed breakdown is clearly needed.
        - Tone: ${TONE_GUIDE[prefs.tone]}.${focus ? `\n        - ${focus}` : ""}
        - ${matureLine(prefs)}`
    });

    const chat = model.startChat({
        history: history.map(h => ({
            role: h.role === "assistant" ? "model" : h.role,
            parts: [{ text: h.content }]
        }))
    });

    const result = await chat.sendMessage(message);

    return result.response.text();
};

export { summarizeGameReviews, chatAboutGame };