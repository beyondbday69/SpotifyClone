# Claude MCP Server Instructions: SpotifyClone

Welcome to the SpotifyClone Android project! You are connected to a remote machine via an MCP Server. Please follow these critical guidelines to maximize efficiency, minimize token usage, and use the provided tools perfectly.

## 1. Do Not Blindly Read Files
When exploring the project, **do not** use `list_spotify_files` or `read_spotify_file` aggressively just to explore the file tree. This consumes massive amounts of your context tokens and takes too much time. 

## 2. Using `search_spotify_code` (The Most Important Tool)
Before reading *any* file, use the **`search_spotify_code`** tool to find exactly what you are looking for. It uses `grep` under the hood.
* **Usage**: Provide a `query` (e.g., `class MainActivity` or `R.layout.activity_main`). You can optionally provide a `directory` (e.g., `app/src/main/java`) to narrow down the search.
* **Why**: This prevents you from reading thousands of lines of unrelated code. Once you find the exact file and line from the search results, *then* you can use `read_spotify_file` to read just that file.

## 3. Using `list_spotify_files`
* **Usage**: Provide a relative `subDir` (e.g., `app/src/main/res`). If you want to see the root directory, leave it empty.
* **Best Practice**: Only use this if you need to see the exact name of a resource file, image, or module directory. Otherwise, prefer `search_spotify_code`.

## 4. Using `read_spotify_file`
* **Usage**: Provide the relative `filePath` (e.g., `app/build.gradle.kts`).
* **Best Practice**: Only read a file if you are absolutely certain it contains the code you need to analyze or modify.

## 5. Using `edit_spotify_file`
* **Usage**: Provide the `filePath` and the complete `content` to overwrite the file.
* **CRITICAL WARNING**: This tool **OVERWRITES** the entire file. You must provide the *entire* updated content of the file. Do not provide a partial snippet, or you will accidentally truncate the file and break the project.

## 6. Using `get_git_diff`
* **Usage**: Call this tool with no arguments to see all changes made to the project, or provide an optional `filePath` to see changes for a specific file.
* **Best Practice**: You **must** use this tool before pushing to GitHub to review your changes line-by-line and ensure you haven't introduced formatting errors, missing imports, or truncated code.

## 7. Using `push_to_github`
* **Usage**: Provide the `branch` you are working on and a descriptive `commitMessage`.
* **Best Practice**: Only call this *after* you have verified your changes with `get_git_diff` and the user confirms they want to push. This tool will automatically stage all changes, commit them, create the branch if it doesn't exist, and push to the remote repository.

## 8. Using `send_agy_prompt`
* **Usage**: Provide a `prompt` string.
* **Best Practice**: If you need to delegate a complex task, ask a question about the server environment, run a terminal command, or trigger something outside the SpotifyClone directory, you can use this tool to send instructions directly to the Antigravity CLI agent running on the machine. This agent has full terminal access and can run scripts for you. Note that it may take a minute to respond, as it is a fully autonomous AI agent.
