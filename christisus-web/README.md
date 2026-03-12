# Christisus Web - Class Planner

React web app that mirrors the Android class planner functionality. Import student data from Excel, configure classes, and generate class assignments with friend/non-friend constraints.

## Prerequisites

- **Node.js** 18+ (includes npm)
  ```bash
  brew install node   # macOS
  ```

## Local Development

```bash
# Install dependencies
npm install

# Start dev server (http://localhost:5173)
npm run dev
```

## Build for Production

```bash
npm run build
```

Output: `dist/` folder with static assets (HTML, JS, CSS).

## Deployment

The app is a static SPA. Deploy the `dist/` folder to any static host.

### Vercel

```bash
npm i -g vercel
vercel
```

Or connect the repo in [vercel.com](https://vercel.com) — it auto-detects Vite.

### Netlify

```bash
npm i -g netlify-cli
npm run build
netlify deploy --prod --dir=dist
```

Or drag-and-drop the `dist/` folder at [app.netlify.com](https://app.netlify.com).

### GitHub Pages

1. Push the repo to GitHub (already at `manikantagarikipati/Christisus`).
2. Deploy:
   ```bash
   cd christisus-web
   npm run deploy
   ```
3. In GitHub: **Settings → Pages** → Source: **Deploy from branch** → Branch: **gh-pages** → Save.
4. Live at `https://manikantagarikipati.github.io/Christisus/`

### Any Static Host (S3, Firebase, etc.)

```bash
npm run build
# Upload contents of dist/ to your host
```

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start dev server |
| `npm run build` | Production build → `dist/` |
| `npm run preview` | Preview production build locally |
