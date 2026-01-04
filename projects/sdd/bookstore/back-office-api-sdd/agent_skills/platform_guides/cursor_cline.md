# Cursor/Cline Platform Guide

## Overview

This guide explains how to use the Jakarta EE Microservice Implementation Agent Skill with:
- **Cursor IDE** - AI-powered code editor with built-in Claude
- **Cline** - VS Code extension for AI-assisted coding with Claude

Both platforms use the same `@` reference syntax for file context.

## Prerequisites

### For Cursor
- Cursor IDE installed
- Claude AI enabled in Cursor
- Project workspace opened in Cursor

### For Cline
- Visual Studio Code installed
- Cline extension installed from VS Code Marketplace
- Claude API key configured in Cline
- Project workspace opened in VS Code

## Using the Agent Skill

### Step 1: Navigate to Agent Skills Directory

Open the Agent Skills folder in your project:
```
projects/sdd/bookstore/berry-books-api-sdd/agent_skills/
```

### Step 2: Select Instruction

Choose the appropriate instruction file:
- **Task Generation**: `instructions/ja/task_generation.md` (Japanese) or `instructions/en/task_generation.md` (English)
- **Code Implementation**: `instructions/ja/code_implementation.md` (Japanese) or `instructions/en/code_implementation.md` (English)

### Step 3: Use @ Reference

In Cursor's chat interface or Cline's chat panel, reference the instruction file using `@`:

**Example (Task Generation):**
```
@agent_skills/instructions/ja/task_generation.md

Please generate implementation tasks for this project.

Parameters:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- spec_directory: projects/sdd/bookstore/berry-books-api-sdd/specs
- output_directory: projects/sdd/bookstore/berry-books-api-sdd/tasks
```

**Example (Code Implementation):**
```
@agent_skills/instructions/ja/code_implementation.md

Please implement the tasks in this file.

Parameters:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- task_file: projects/sdd/bookstore/berry-books-api-sdd/tasks/setup_tasks.md
```

### Step 4: AI Will Use Available Tools

Claude in Cursor has access to:
- `read_file` - Read specification and task files
- `write` - Create new files
- `search_replace` - Edit existing files
- `grep` - Search code
- `codebase_search` - Semantic search
- `run_terminal_cmd` - Execute commands
- `list_dir` - List directory contents

The AI will automatically use these tools to:
1. Read specification documents
2. Analyze project structure
3. Generate task files or implement code
4. Run tests and verify implementation

## Platform-Specific Features

### File Access
Claude can directly read files using `read_file` tool:
- No need to manually paste file contents
- Can read multiple files in parallel
- Supports both text and image files

### Code Editing
Claude can edit files using `search_replace` tool:
- Precise string replacement
- Handles indentation correctly
- Shows diff before applying changes

### Terminal Commands
Claude can execute terminal commands:
- Run build commands (`./gradlew build`)
- Execute tests (`./gradlew test`)
- Run the application
- Git operations

### Codebase Search
Claude can search the codebase semantically:
- Find implementations by description
- Locate specific patterns
- Understand code context

## Best Practices

### 1. Reference Multiple Files
You can reference multiple files in one request:
```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_auth.md

Please implement this API
```

### 2. Use Composer for Complex Tasks
For multi-step tasks, use Cursor's Composer feature:
- Opens a dedicated panel
- Better for long conversations
- Can track progress across multiple files

### 3. Enable Agent Mode
Switch to Agent mode for:
- File creation and editing
- Terminal command execution
- Multi-step implementations

### 4. Reference Specifications
Reference spec files directly:
```
@specs/baseline/system/architecture_design.md
@specs/baseline/api/API_001_auth/functional_design.md

Implement authentication based on these specs
```

### 5. Incremental Implementation
Implement one task file at a time:
```
@agent_skills/instructions/ja/code_implementation.md
@tasks/setup_tasks.md

Please execute setup tasks only. Stop when complete.
```

## Common Workflows

### Workflow 1: Generate All Tasks

```
@agent_skills/instructions/ja/task_generation.md

Generate complete task list for this project.

Parameters:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- spec_directory: projects/sdd/bookstore/berry-books-api-sdd/specs
- output_directory: projects/sdd/bookstore/berry-books-api-sdd/tasks
```

### Workflow 2: Implement One API

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_auth.md

Implement Authentication API.

Parameters:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- task_file: projects/sdd/bookstore/berry-books-api-sdd/tasks/API_001_auth.md
```

### Workflow 3: Parallel Implementation (Team)

Team Member A:
```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_auth.md

Implement Auth API
```

Team Member B (simultaneously):
```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_002_books.md

Implement Book API
```

### Workflow 4: Fix and Test

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_auth.md

All tasks are marked complete, but tests are failing.
Please review test failures and fix issues.
```

## Troubleshooting

### Issue: Claude can't find files

**Solution**: Use absolute or workspace-relative paths:
```
Correct: projects/sdd/bookstore/berry-books-api-sdd/specs/...
Incorrect: ../specs/...
```

### Issue: Changes not applied

**Solution**: Make sure you're in Agent mode, not Ask mode:
- Check mode toggle at the bottom of chat
- Switch to Agent mode to allow file modifications

### Issue: Too many files to read

**Solution**: Be specific about which files to read:
```
Please read only:
- architecture_design.md
- API_001_auth/functional_design.md

Then implement authentication.
```

### Issue: Task execution too fast

**Solution**: Request step-by-step execution:
```
Execute tasks one at a time.
After each task, stop and wait for my confirmation.
```

## Tips for Better Results

1. **Be Explicit with Parameters**: Always specify project_root clearly
2. **Reference Relevant Specs**: @ reference specific specification files
3. **Use Task Markers**: Update task checkboxes [X] as you progress
4. **Verify Before Proceeding**: Ask Claude to verify completion before next task
5. **Use Git**: Commit after each major milestone
6. **Review Changes**: Use Cursor's diff view to review AI changes

## Keyboard Shortcuts (Cursor)

- `Cmd/Ctrl + L` - Open chat
- `Cmd/Ctrl + K` - Quick edit
- `Cmd/Ctrl + Shift + L` - Open Composer
- `Cmd/Ctrl + I` - Open inline edit

## Additional Resources

- [Cursor Documentation](https://docs.cursor.sh/)
- [Claude in Cursor](https://docs.cursor.sh/get-started/migrate-from-vs-code)
- [Agent Skills Specification](https://github.com/agentskills/agentskills)

