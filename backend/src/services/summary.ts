import prisma from "../prisma/client";
import { searchGameReviews } from "./tavily";
import { summarizeGameReviews, GameSummary } from "./gemini";
import { UserPreferences } from "./preferences";

const SUMMARY_TTL_MS = 1000 * 60 * 60; // 1 hour

// Returns a cached summary if one exists and is under an hour old; otherwise generates a fresh
// one (Tavily + Gemini), stores it, and returns it. `forceRefresh` always regenerates.
export const getOrCreateSummary = async (
    userId: number,
    rawgId: number,
    gameName: string,
    prefs: UserPreferences,
    forceRefresh: boolean
): Promise<GameSummary> => {
    if (!forceRefresh) {
        const cached = await prisma.summaryCache.findUnique({
            where: { userId_rawgId: { userId, rawgId } },
        });
        if (cached && Date.now() - cached.createdAt.getTime() < SUMMARY_TTL_MS) {
            return { summary: cached.summary, sources: cached.sources };
        }
    }

    const reviews = await searchGameReviews(gameName);
    const summary = await summarizeGameReviews(gameName, reviews, prefs);

    await prisma.summaryCache.upsert({
        where: { userId_rawgId: { userId, rawgId } },
        create: { userId, rawgId, summary: summary.summary, sources: summary.sources },
        update: { summary: summary.summary, sources: summary.sources, createdAt: new Date() },
    });

    return summary;
};
