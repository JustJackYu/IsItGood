import { GoogleGenerativeAI } from "@google/generative-ai"

export interface GameSummary {
    summary: string;
    sources: string[];
}

const summarizeGameReviews = async (game: string, snippets: Array<{ content: string; url: string }>): Promise<GameSummary> => {
    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
        throw new Error("GEMINI API Key is missing");
    }

    const genAI = new GoogleGenerativeAI(apiKey);
    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

    const reviewText = snippets.map(s => s.content).join("\n\n")

    const prompt = `You are a game reviewer. Summarize the reviews of ${game}.
    Answer these questions using these review snippets: ${reviewText}
    1. Is the game fun?
    2. Is the game worth buying?
    Answer in a concise and clear manner.
    `

    const response = await model.generateContent(prompt);

    const summary: GameSummary = {
        summary: response.response.text(),
        sources: snippets.map(s => s.url)
    }

    return summary;
}

export { summarizeGameReviews };