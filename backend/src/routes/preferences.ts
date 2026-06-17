import { Router, Request, Response } from "express";
import authMiddleware from "../middleware/auth";
import prisma from "../prisma/client";
import {
    DEFAULT_PREFERENCES,
    SUMMARY_LENGTHS,
    TONES,
    FONT_SIZES,
    DEAL_DISPLAYS,
} from "../services/preferences";

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
        const {
            summaryLength, tone, lookOutFor, allowMatureContent, fontSize,
            dealDisplay, saleAlertDiscount, saleAlertPrice, chatLeaveWarning,
        } = req.body;

        const data: Record<string, unknown> = {};

        if (summaryLength !== undefined) {
            if (!SUMMARY_LENGTHS.includes(summaryLength)) {
                return res.status(400).json({ message: "Invalid summaryLength" });
            }
            data.summaryLength = summaryLength;
        }

        if (tone !== undefined) {
            if (!TONES.includes(tone)) {
                return res.status(400).json({ message: "Invalid tone" });
            }
            data.tone = tone;
        }

        if (fontSize !== undefined) {
            if (!FONT_SIZES.includes(fontSize)) {
                return res.status(400).json({ message: "Invalid fontSize" });
            }
            data.fontSize = fontSize;
        }

        if (lookOutFor !== undefined) {
            if (!Array.isArray(lookOutFor) || !lookOutFor.every((x) => typeof x === "string")) {
                return res.status(400).json({ message: "lookOutFor must be an array of strings" });
            }
            data.lookOutFor = lookOutFor;
        }

        if (allowMatureContent !== undefined) {
            if (typeof allowMatureContent !== "boolean") {
                return res.status(400).json({ message: "allowMatureContent must be a boolean" });
            }
            data.allowMatureContent = allowMatureContent;
        }

        if (chatLeaveWarning !== undefined) {
            if (typeof chatLeaveWarning !== "boolean") {
                return res.status(400).json({ message: "chatLeaveWarning must be a boolean" });
            }
            data.chatLeaveWarning = chatLeaveWarning;
        }

        if (dealDisplay !== undefined) {
            if (!DEAL_DISPLAYS.includes(dealDisplay)) {
                return res.status(400).json({ message: "Invalid dealDisplay" });
            }
            data.dealDisplay = dealDisplay;
        }

        // Thresholds accept null (to clear the alert) or a non-negative number.
        if (saleAlertDiscount !== undefined) {
            if (saleAlertDiscount !== null &&
                (typeof saleAlertDiscount !== "number" || saleAlertDiscount < 0 || saleAlertDiscount > 100)) {
                return res.status(400).json({ message: "saleAlertDiscount must be null or a number between 0 and 100" });
            }
            data.saleAlertDiscount = saleAlertDiscount;
        }

        if (saleAlertPrice !== undefined) {
            if (saleAlertPrice !== null && (typeof saleAlertPrice !== "number" || saleAlertPrice < 0)) {
                return res.status(400).json({ message: "saleAlertPrice must be null or a non-negative number" });
            }
            data.saleAlertPrice = saleAlertPrice;
        }

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
