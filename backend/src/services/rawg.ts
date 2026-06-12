interface Game {
    id: number;
    name: string;
    background_image: string;
    rating: number;
    released: string;
}

const searchGames = async (query: string): Promise<Game[]> => {
    const RAWG_API_KEY = process.env.RAWG_API_KEY;
    if (!RAWG_API_KEY) {
        throw new Error("RAWG API Key is missing");
    }

    const searchUrl = `https://api.rawg.io/api/games?key=${RAWG_API_KEY}&search=${encodeURIComponent(query)}&page_size=20&ordering=-added`;
    const response = await fetch(searchUrl);

    if (!response.ok) {
        throw new Error(`Failed to search games: ${response.statusText}`);
    }

    const data = await response.json();

    return (data.results as Game[]).slice(0, 10);
};

const fetchGameById = async (id: number): Promise<Game> => {
    const RAWG_API_KEY = process.env.RAWG_API_KEY;
    if (!RAWG_API_KEY) {
        throw new Error("RAWG API Key is missing");
    }

    const url = `https://api.rawg.io/api/games/${id}?key=${RAWG_API_KEY}`;
    const response = await fetch(url);

    if (!response.ok) {
        throw new Error(`Failed to fetch game: ${response.statusText}`);
    }

    const data = await response.json();

    return {
        id: data.id,
        name: data.name,
        background_image: data.background_image,
        rating: data.rating,
        released: data.released
    };
};

interface TopGame {
    id: number;
    name: string;
    background_image: string | null;
    rating: number | null;
    added: number;
    released: string | null;
}

// Returns the single best RAWG match for a title, including `added` (popularity) — used to
// enrich and rank external deals. Returns null when nothing matches.
const findTopGame = async (query: string): Promise<TopGame | null> => {
    const RAWG_API_KEY = process.env.RAWG_API_KEY;
    if (!RAWG_API_KEY) {
        throw new Error("RAWG API Key is missing");
    }

    const url = `https://api.rawg.io/api/games?key=${RAWG_API_KEY}&search=${encodeURIComponent(query)}&search_precise=true&page_size=1`;
    const response = await fetch(url);

    if (!response.ok) {
        return null;
    }

    const data = await response.json();
    const game = data?.results?.[0];
    if (!game) {
        return null;
    }

    return {
        id: game.id,
        name: game.name,
        background_image: game.background_image ?? null,
        rating: game.rating ?? null,
        added: game.added ?? 0,
        released: game.released ?? null,
    };
};

export { searchGames, fetchGameById, findTopGame };