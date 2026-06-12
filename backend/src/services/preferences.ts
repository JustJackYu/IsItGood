import prisma from "../prisma/client";

export interface UserPreferences {
    summaryLength: "SHORT" | "MEDIUM" | "LONG";
    tone: "CASUAL" | "BALANCED" | "CRITICAL" | "ENTHUSIASTIC";
    lookOutFor: string[];
    allowMatureContent: boolean;
    fontSize: "SMALL" | "MEDIUM" | "LARGE";
    dealDisplay: "PRICE" | "DISCOUNT" | "BOTH";
    saleAlertDiscount: number | null;
    saleAlertPrice: number | null;
}

export const SUMMARY_LENGTHS: UserPreferences["summaryLength"][] = ["SHORT", "MEDIUM", "LONG"];
export const TONES: UserPreferences["tone"][] = ["CASUAL", "BALANCED", "CRITICAL", "ENTHUSIASTIC"];
export const FONT_SIZES: UserPreferences["fontSize"][] = ["SMALL", "MEDIUM", "LARGE"];
export const DEAL_DISPLAYS: UserPreferences["dealDisplay"][] = ["PRICE", "DISCOUNT", "BOTH"];

export const DEFAULT_PREFERENCES: UserPreferences = {
    summaryLength: "MEDIUM",
    tone: "BALANCED",
    lookOutFor: [],
    allowMatureContent: false,
    fontSize: "MEDIUM",
    dealDisplay: "BOTH",
    saleAlertDiscount: null,
    saleAlertPrice: null,
};

// Returns the user's stored preferences, or sensible defaults if they haven't set any yet.
export const getEffectivePreferences = async (userId: number): Promise<UserPreferences> => {
    const prefs = await prisma.userPreferences.findUnique({ where: { userId } });
    if (!prefs) return DEFAULT_PREFERENCES;

    return {
        summaryLength: prefs.summaryLength,
        tone: prefs.tone,
        lookOutFor: prefs.lookOutFor,
        allowMatureContent: prefs.allowMatureContent,
        fontSize: prefs.fontSize,
        dealDisplay: prefs.dealDisplay,
        saleAlertDiscount: prefs.saleAlertDiscount,
        saleAlertPrice: prefs.saleAlertPrice,
    };
};
