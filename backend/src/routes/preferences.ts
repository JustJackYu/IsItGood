import { Router, Request, Response } from "express";
import authMiddleware from "../middleware/auth";
import prisma from "../prisma/client";
import { DEFAULT_PREFERENCES, validatePreferencesPatch } from "../services/preferences";

interface AuthRequest extends Request {
    user?: {
        id: number;
        email: string;
    }
}

const router = Router();

router.get("/", authMiddleware, async (req: AuthRequest, res: Response) => {
    try {
        const userId = req.user!.id;
        const prefs = await prisma.userPreferences.findUnique({ where: { userId } });
        return res.json(prefs ?? DEFAULT_PREFERENCES);
    } catch (error) {
        console.error("Error fetching preferences:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
});

router.put("/", authMiddleware, async (req: AuthRequest, res: Response) => {
    try {
        const userId = req.user!.id;

        const result = validatePreferencesPatch(req.body);
        if ("error" in result) {
            return res.status(400).json({ message: result.error });
        }
        const data = result.data;

        const prefs = await prisma.userPreferences.upsert({
            where: { userId },
            create: { userId, ...data },
            update: data,
        });

        return res.json(prefs);
    } catch (error) {
        console.error("Error updating preferences:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
});

export default router;
