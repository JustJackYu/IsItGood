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
}

export { searchGames };