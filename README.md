# Hugo Sell – 1.21.11 Fabric

This project is prepared so GitHub Actions can build the mod without installing Java/Gradle locally.

## Easiest way

1. Create a new GitHub repository.
2. Upload **all files in this folder** to the repository.
3. Open the repository's **Actions** tab.
4. Select **Build Hugo Sell**.
5. Click **Run workflow**.
6. When it finishes, open the workflow run and download the artifact named **hugo-sell-jar**.
7. Put the `.jar` into `%appdata%\\.minecraft\\mods`.

Target: Minecraft 1.21.11 + Fabric + Java 21.

Note: the mod automates the server's `/sell` GUI based on the Hugo SMP GUI layout supplied by the user. The server remains responsible for the actual sale.
