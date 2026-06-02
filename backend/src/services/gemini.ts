import { GoogleGenerativeAI } from "@google/generative-ai"

export interface GameSummary {
    summary: string;
    sources: string[];
}



const summarizeGameReviews = async (game: string, snippets: Array<{ content: string; url: string }>): Promise<GameSummary> => {
    const GEMINI_API_KEY = process.env.GEMINI_API_KEY;

    if (!GEMINI_API_KEY) {
        throw new Error("GEMINI API Key is missing");
    }

    const genAi = new GoogleGenerativeAI(GEMINI_API_KEY);
    const model = genAi.getGenerativeModel({ model: "gemini-2.5-flash" });

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

const chatAboutGame = async (message: string, gameTitle: string, gameSummary: string, history: { role: string, content: string }[]): Promise<string> => {
    const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
    const genAi = new GoogleGenerativeAI(GEMINI_API_KEY!);
    const model = genAi.getGenerativeModel({
        model: "gemini-2.5-flash",
        systemInstruction: `"You are a game expert assistant. The user is asking about ${gameTitle}. 
            Here is a summary from reviews: ${gameSummary}. 
            Answer questions based on this context."`
    });

    const chat = model.startChat({
        history: history.map(h => ({
            role: h.role,
            parts: [{ text: h.content }]
        }))
    })

    const result = await chat.sendMessage(message);

    return result.response.text();
}

export { summarizeGameReviews, chatAboutGame };