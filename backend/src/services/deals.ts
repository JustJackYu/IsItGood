import { findTopGame } from "./rawg";
import { getBestDeal, BestDeal } from "./itad";

export interface DealGame {
    rawgId: number;
    title: string;
    coverImage: string | null;
    rating: number | null;
    discountPercent: number;
    price: number;
    regularPrice: number;
    currency: string;
    store: string;
    url: string;
}

const ITAD_BASE = "https://api.isthereanydeal.com";
const COUNTRY = "CA";
const MIN_CUT = 50;        // only "great sales" — at least 50% off
const POOL_LIMIT = 60;     // trending deals to pull before filtering
const ENRICH_CAP = 30;     // cap RAWG lookups per refresh to bound cost
const RESULT_LIMIT = 10;
const CACHE_TTL_MS = 1000 * 60 * 60 * 6; // 6 hours — deals change slowly

// Deals are expensive to assemble (1 ITAD call + N RAWG lookups), so cache the finished result.
let cache: { data: DealGame[]; expires: number } | null = null;

interface EnrichedDeal extends DealGame {
    added: number; // RAWG popularity, used only for ranking
}

export const getPopularDeals = async (): Promise<DealGame[]> => {
    if (cache && cache.expires > Date.now()) {
        return cache.data;
    }

    const API_KEY = process.env.ITAD_API_KEY;
    if (!API_KEY) {
        throw new Error("ITAD API Key is missing");
    }

    const res = await fetch(
        `${ITAD_BASE}/deals/v2?key=${API_KEY}&country=${COUNTRY}&sort=-trending&limit=${POOL_LIMIT}`
    );
    if (!res.ok) {
        throw new Error(`ITAD deals request failed: ${res.status}`);
    }

    const data = await res.json();
    const list: any[] = Array.isArray(data?.list) ? data.list : [];

    const candidates = list
        .filter((item) => typeof item?.deal?.cut === "number" && item.deal.cut >= MIN_CUT)
        .slice(0, ENRICH_CAP);

    // Enrich with RAWG in parallel; individual failures just drop that deal.
    const enriched = await Promise.all(
        candidates.map(async (item): Promise<EnrichedDeal | null> => {
            try {
                const rawg = await findTopGame(item.title);
                if (!rawg) return null;
                return {
                    rawgId: rawg.id,
                    title: item.title,
                    coverImage: item.assets?.boxart ?? rawg.background_image ?? null,
                    rating: rawg.rating,
                    discountPercent: item.deal.cut,
                    price: item.deal.price?.amount ?? 0,
                    regularPrice: item.deal.regular?.amount ?? 0,
                    currency: item.deal.price?.currency ?? "CAD",
                    store: item.deal.shop?.name ?? "",
                    url: item.deal.url ?? "",
                    added: rawg.added,
                };
            } catch {
                return null;
            }
        })
    );

    // Dedupe by game (multiple editions can map to one RAWG id) — keep the steepest discount.
    const byId = new Map<number, EnrichedDeal>();
    for (const deal of enriched) {
        if (!deal) continue;
        const existing = byId.get(deal.rawgId);
        if (!existing || deal.discountPercent > existing.discountPercent) {
            byId.set(deal.rawgId, deal);
        }
    }

    const result: DealGame[] = [...byId.values()]
        .sort((a, b) => b.added - a.added) // rank by RAWG popularity
        .slice(0, RESULT_LIMIT)
        .map(({ added, ...deal }) => deal);

    cache = { data: result, expires: Date.now() + CACHE_TTL_MS };
    return result;
};

export interface SavedGameDeal {
    rawgId: number;
    deal: BestDeal | null;
}

// Per-title best-deal cache, shared across users (deals don't depend on who's asking).
const savedDealCache = new Map<string, { deal: BestDeal | null; expires: number }>();

const cachedBestDeal = async (title: string): Promise<BestDeal | null> => {
    const hit = savedDealCache.get(title);
    if (hit && hit.expires > Date.now()) {
        return hit.deal;
    }
    const deal = await getBestDeal(title).catch(() => null);
    savedDealCache.set(title, { deal, expires: Date.now() + CACHE_TTL_MS });
    return deal;
};

// Looks up the current best deal for each saved game so the client can flag threshold matches.
export const getDealsForSavedGames = async (
    games: { rawgId: number; title: string }[]
): Promise<SavedGameDeal[]> => {
    return Promise.all(
        games.map(async (g) => ({
            rawgId: g.rawgId,
            deal: await cachedBestDeal(g.title),
        }))
    );
};
