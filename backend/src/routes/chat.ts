import { Router, Request, Response } from "express";
import { chatAboutGame } from "../services/gemini";

const router = Router();

router.post('/', async (req: Request, res: Response) => {
    const { message, gameTitle, summary, history } = req.body;

    if (!message || !gameTitle || !summary || !history) {
        return res.status(400).json({ message: "Missing required fields" });
    }

    if (!Array.isArray(history)) {
        return res.status(400).json({ message: "History is invalid" });
    }

    try {
        const response = await chatAboutGame(message, gameTitle, summary, history);
        return res.json({ reply: response });
    }
    catch (error) {
        console.error("Error chatting about game:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
})

export default router;