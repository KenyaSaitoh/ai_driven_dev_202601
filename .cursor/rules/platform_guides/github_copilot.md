# GitHub Copilot Platform Guide

## Overview

This guide explains how to use the Jakarta EE BFF Implementation Agent Skill with GitHub Copilot in VS Code or JetBrains IDEs.

## Prerequisites

- VS Code or JetBrains IDE installed
- GitHub Copilot subscription active
- GitHub Copilot Chat extension installed
- Project workspace opened

## Using the Agent Skill

### Step 1: Open Copilot Chat

**VS Code:**
- Click Copilot icon in sidebar
- Or press `Ctrl + Shift + I` (Windows/Linux) or `Cmd + Shift + I` (Mac)

**JetBrains:**
- Click Copilot icon in sidebar
- Or use menu: Tools → GitHub Copilot → Open Chat

### Step 2: Reference Instruction File

Use `#file` to reference the instruction:

**Example (Task Generation):**
```
#file:agent_skills/instructions/en/task_generation.md

Please generate implementation tasks for this project.

Parameters:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- spec_directory: projects/sdd/bookstore/berry-books-api-sdd/specs
- output_directory: projects/sdd/bookstore/berry-books-api-sdd/tasks

Please also reference:
#file:specs/baseline/system/architecture_design.md
#file:specs/baseline/system/functional_design.md
```

**Example (Code Implementation):**
```
#file:agent_skills/instructions/en/code_implementation.md
#file:tasks/API_001_auth.md

Please implement Authentication API.

Parameters:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- task_file: projects/sdd/bookstore/berry-books-api-sdd/tasks/API_001_auth.md

Also reference:
#file:specs/baseline/api/API_001_auth/functional_design.md
#file:specs/baseline/system/architecture_design.md
```

### Step 3: Provide Context with @workspace

Use `@workspace` to give Copilot access to workspace context:

```
@workspace #file:agent_skills/instructions/en/code_implementation.md

Implement setup tasks based on specifications in this workspace.
```

### Step 4: Iterative Implementation

GitHub Copilot works best with iterative requests:

1. **Request file creation:**
```
Create LoginRequest.java based on:
#file:specs/baseline/api/API_001_auth/functional_design.md
Section "2.1 LoginRequest"
```

2. **Review and accept** the suggested code

3. **Request next file:**
```
Create AuthenResource.java based on:
#file:specs/baseline/api/API_001_auth/functional_design.md
Section "3. Endpoint Specifications"
```

## Platform-Specific Features

### File References (#file)
Copilot can access files using `#file:`:
```
#file:path/to/file.md
```
- Must use forward slashes even on Windows
- Path relative to workspace root
- Can reference multiple files

### Workspace Context (@workspace)
Use `@workspace` for broader context:
```
@workspace Where is JWT authentication configured?
```

### Code Suggestions
Copilot provides inline code suggestions:
- Start typing and Copilot suggests completions
- Press `Tab` to accept
- Press `Alt + ]` for next suggestion
- Press `Alt + [` for previous suggestion

### Slash Commands
Use slash commands in chat:
- `/explain` - Explain selected code
- `/fix` - Suggest fixes for problems
- `/new` - Create new file
- `/tests` - Generate tests

## Recommended Workflow

### Phase 1: Task Generation (Semi-Manual)

Since Copilot doesn't automatically create multiple files, task generation is semi-manual:

1. **Read instruction:**
```
#file:agent_skills/instructions/en/task_generation.md

Summarize the task generation process.
```

2. **Analyze specs:**
```
@workspace Analyze all specification files and list all APIs to implement.
```

3. **Generate each task file:**
```
Create setup_tasks.md based on:
#file:agent_skills/resources/templates/task_template.md
#file:specs/baseline/system/architecture_design.md

Follow the task generation instruction format.
```

4. **Repeat for each API**

### Phase 2: Code Implementation

1. **Open task file:**
Open `tasks/API_001_auth.md` in editor

2. **Implement each task:**
```
#file:tasks/API_001_auth.md
#file:agent_skills/instructions/en/code_implementation.md

Implement T_API001_001: Create LoginRequest DTO

Reference:
#file:specs/baseline/api/API_001_auth/functional_design.md
```

3. **Use inline suggestions:**
- Start typing class name, Copilot suggests structure
- Accept or modify suggestions

4. **Generate tests:**
```
/tests Generate unit tests for LoginRequest.java
Based on #file:specs/baseline/api/API_001_auth/behaviors.md
```

5. **Mark task complete:**
Manually change `- [ ]` to `- [X]` in task file

### Phase 3: Verification

```
@workspace #file:agent_skills/instructions/en/code_implementation.md

Verify that all tasks in API_001_auth.md are complete.
Check:
- All files exist
- Tests pass
- Specifications are followed
```

## Best Practices

### 1. Be Specific with File References
Always use exact file paths:
```
Good: #file:specs/baseline/system/architecture_design.md
Bad: #file:architecture_design.md
```

### 2. Break Down Large Tasks
Request one component at a time:
```
Create only the JwtUtil class.
Next request: Create JwtAuthenFilter.
```

### 3. Use Comments as Guides
Add TODO comments for Copilot to complete:
```java
// TODO: Implement login method
// - Validate credentials via CustomerHubRestClient
// - Generate JWT token using JwtUtil
// - Set HttpOnly cookie
// - Return success response
public Response login(LoginRequest request) {
    // Copilot will suggest implementation
}
```

### 4. Review Generated Code
Always review Copilot's suggestions:
- Check for correct imports
- Verify error handling
- Ensure proper exception types
- Validate against specifications

### 5. Leverage Inline Chat
Use inline chat (`Ctrl/Cmd + I`) for quick edits:
- Select code
- Press `Ctrl/Cmd + I`
- Type instruction
- Review and accept changes

## Common Workflows

### Workflow 1: Create Single Class

```
#file:agent_skills/instructions/en/code_implementation.md
#file:tasks/API_001_auth.md
#file:specs/baseline/api/API_001_auth/functional_design.md

Create LoginRequest.java for task T_API001_001.
Requirements from spec section "2.1 LoginRequest":
- email field (String, @NotNull, @Email)
- password field (String, @NotNull, @Size(min=8))
```

### Workflow 2: Generate Test

```
/tests

Generate unit test for AuthenResource.java
Test scenarios from:
#file:specs/baseline/api/API_001_auth/behaviors.md

Include:
- Successful login
- Invalid credentials
- Email not found
- Mock CustomerHubRestClient
```

### Workflow 3: Fix Errors

```
/fix

This code has compilation errors.
Fix according to:
#file:specs/baseline/system/architecture_design.md
Use Jakarta EE 10 annotations, not Java EE.
```

### Workflow 4: Explain Code

```
/explain

Explain how this BFF pattern implementation works.
Reference:
#file:agent_skills/resources/architecture_patterns/bff_pattern.md
```

## Limitations and Workarounds

### Limitation 1: Can't Create Multiple Files at Once

**Workaround**: Request one file per chat message:
```
1st request: Create LoginRequest.java
2nd request: Create RegisterRequest.java
3rd request: Create AuthenResource.java
```

### Limitation 2: Limited File Reading

**Workaround**: Provide relevant spec content in chat:
```
According to the specification:
[Paste relevant section]

Create the implementation.
```

### Limitation 3: No Automatic Task Tracking

**Workaround**: Manually update task files:
- Open task file
- Change `- [ ]` to `- [X]` for completed tasks
- Add notes if needed

### Limitation 4: Context Window Limits

**Workaround**: Start new conversation for new APIs:
```
Previous conversation: Auth API ✓
New conversation: Book API
```

## Tips for Better Results

1. **Use Descriptive Variable Names**: Copilot learns from your naming
2. **Write Function Signatures First**: Let Copilot complete implementation
3. **Add Type Hints**: Copilot uses types for better suggestions
4. **Reference Similar Code**: Point Copilot to existing implementations
5. **Use Design Patterns**: Mention pattern names (e.g., "Factory pattern")

## Keyboard Shortcuts

### VS Code
- `Ctrl/Cmd + Shift + I` - Open Copilot Chat
- `Ctrl/Cmd + I` - Inline Chat
- `Tab` - Accept suggestion
- `Alt + ]` - Next suggestion
- `Alt + [` - Previous suggestion

### JetBrains
- `Alt + \` - Show inline suggestions
- `Tab` - Accept suggestion
- `Alt + ]` - Next suggestion
- `Alt + [` - Previous suggestion

## Integration with Build Tools

### Run Tests
```
@terminal ./gradlew test

Explain test failures and suggest fixes.
```

### Build Project
```
@terminal ./gradlew build

Fix any compilation errors found.
```

### Run Application
```
@terminal ./gradlew bootRun

Check if application starts successfully.
```

## Additional Resources

- [GitHub Copilot Documentation](https://docs.github.com/en/copilot)
- [VS Code Copilot](https://marketplace.visualstudio.com/items?itemName=GitHub.copilot)
- [JetBrains Copilot](https://plugins.jetbrains.com/plugin/17718-github-copilot)
- [Agent Skills Specification](https://github.com/agentskills/agentskills)

