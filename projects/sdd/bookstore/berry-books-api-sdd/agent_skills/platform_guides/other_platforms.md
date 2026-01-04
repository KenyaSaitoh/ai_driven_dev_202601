# Other Platforms Guide

## Overview

This guide explains how to use the Jakarta EE BFF Implementation Agent Skill with other AI coding assistants and platforms not specifically covered in dedicated guides.

## Supported Platforms

This Agent Skill is designed to work with any AI platform that can:
- Read text files
- Understand instructions
- Generate code
- Follow structured processes

### Confirmed Compatible Platforms

- **ChatGPT (OpenAI)** - Via web interface or API
- **Claude (Anthropic)** - Via web interface (claude.ai) or API
- **Gemini (Google)** - Via web interface or API
- **Custom AI Solutions** - Via API integration
- **Local LLMs** - Via Ollama, LM Studio, etc.

## General Usage Pattern

### Step 1: Prepare Context

Gather the following files:
1. Instruction file (`instructions/{language}/task_generation.md` or `code_implementation.md`)
2. Relevant specification files
3. Existing task files (for code implementation)
4. Project structure information

### Step 2: Provide Instructions

Copy and paste the instruction file content into your AI interface, along with parameters:

```
[Paste instruction file content]

Please execute this instruction with the following parameters:

Parameters:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- spec_directory: projects/sdd/bookstore/berry-books-api-sdd/specs
- output_directory: projects/sdd/bookstore/berry-books-api-sdd/tasks
```

### Step 3: Provide Additional Context

As requested by the AI, provide:
- Specification file contents
- Existing code
- Task file contents
- Error messages or logs

### Step 4: Implement Changes

The AI will provide:
- File contents to create
- Code changes to apply
- Commands to execute

You manually:
- Create/edit files
- Run commands
- Verify results

## Platform-Specific Instructions

### ChatGPT (Web Interface)

**Advantages:**
- Good at understanding complex instructions
- Can maintain context across multiple messages
- Supports file uploads (Plus/Team/Enterprise)

**Usage:**

1. **Upload instruction file** (if available):
   - Click paperclip icon
   - Upload `task_generation.md` or `code_implementation.md`

2. **Or paste instruction:**
```
I'm using the Jakarta EE BFF Implementation Agent Skill.

[Paste instruction content]

Parameters:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- spec_directory: projects/sdd/bookstore/berry-books-api-sdd/specs
```

3. **Provide specifications as needed:**
```
Here is the architecture_design.md:

[Paste content]
```

4. **Request output:**
```
Generate the task files as specified.
Provide complete file contents for each task file.
```

5. **Copy and save** each generated file manually

**Limitations:**
- No direct file access
- No automatic file creation
- Manual copy-paste required
- Context window limits

### Claude (Web Interface - claude.ai)

**Advantages:**
- Large context window (100K+ tokens)
- Good at following complex instructions
- Can handle entire specifications

**Usage:**

1. **Start conversation with full context:**
```
I'm using the Jakarta EE BFF Implementation Agent Skill for task generation.

Instruction:
[Paste full instruction]

Specifications:
[Paste architecture_design.md]
[Paste functional_design.md]
[Paste API specifications]

Parameters:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- spec_directory: projects/sdd/bookstore/berry-books-api-sdd/specs
- output_directory: projects/sdd/bookstore/berry-books-api-sdd/tasks

Please analyze and generate all task files.
```

2. **Claude will generate** complete task files

3. **Copy each file** and save locally

**Tip:** Claude can handle large amounts of text, so paste multiple specs at once.

### Gemini (Google AI)

**Advantages:**
- Good code understanding
- Supports multiple languages
- Free tier available

**Usage:**

1. **Provide instruction and context:**
```
Agent Skill: Jakarta EE BFF Implementation

[Paste instruction]

Project specifications:
[Paste relevant specs]

Please generate implementation tasks.
```

2. **Iterate as needed:**
```
Now generate the Authentication API task file (API_001_auth.md)
based on specs/baseline/api/API_001_auth/functional_design.md
```

3. **Save outputs manually**

### API Integration (OpenAI, Anthropic, Google)

For programmatic usage:

**Python Example (OpenAI):**
```python
import openai

# Read instruction
with open('agent_skills/instructions/en/task_generation.md') as f:
    instruction = f.read()

# Read specs
with open('specs/baseline/system/architecture_design.md') as f:
    architecture = f.read()

# Call API
response = openai.ChatCompletion.create(
    model="gpt-4",
    messages=[
        {"role": "system", "content": instruction},
        {"role": "user", "content": f"""
Generate tasks for berry-books-api.

Architecture:
{architecture}

Parameters:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- output_directory: projects/sdd/bookstore/berry-books-api-sdd/tasks
"""}
    ]
)

# Save output
task_content = response.choices[0].message.content
with open('tasks/setup_tasks.md', 'w') as f:
    f.write(task_content)
```

**Node.js Example:**
```javascript
const OpenAI = require('openai');
const fs = require('fs');

const openai = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });

const instruction = fs.readFileSync('agent_skills/instructions/en/task_generation.md', 'utf8');
const architecture = fs.readFileSync('specs/baseline/system/architecture_design.md', 'utf8');

async function generateTasks() {
    const response = await openai.chat.completions.create({
        model: 'gpt-4',
        messages: [
            { role: 'system', content: instruction },
            { role: 'user', content: `Generate tasks. Architecture:\n${architecture}` }
        ]
    });
    
    fs.writeFileSync('tasks/setup_tasks.md', response.choices[0].message.content);
}

generateTasks();
```

### Local LLMs (Ollama, LM Studio)

**Advantages:**
- Privacy (no data sent to cloud)
- No API costs
- Customizable models

**Setup:**

1. **Install Ollama** (or LM Studio)
```bash
# macOS/Linux
curl -fsSL https://ollama.com/install.sh | sh

# Windows
# Download from ollama.com
```

2. **Pull a capable model:**
```bash
ollama pull llama2:70b
# or
ollama pull codellama:34b
```

3. **Use via API or CLI:**
```bash
ollama run llama2:70b "$(cat agent_skills/instructions/en/task_generation.md)"
```

**Limitations:**
- Smaller context window than cloud models
- May need more specific instructions
- Slower generation
- May require breaking down into smaller tasks

### Custom AI Solutions

For custom AI implementations:

1. **Parse skill.yaml** for metadata
2. **Load appropriate instruction** (language, entry point)
3. **Load resources** (BFF pattern docs, templates)
4. **Follow instruction steps** programmatically
5. **Generate outputs** according to specification

## Best Practices for Manual Platforms

### 1. Work in Phases

**Phase 1: Task Generation**
- Generate all task files
- Save locally
- Review completeness

**Phase 2: Implementation**
- Work on one task file at a time
- Implement one task per conversation
- Save and test before next task

### 2. Use Templates

Copy template files and modify:
```
Copy: resources/templates/task_template.md
To: tasks/API_001_auth.md
Then ask AI to fill in details
```

### 3. Maintain Context Files

Create a context file with frequently needed info:
```
context.txt:
- Project: berry-books-api
- Pattern: BFF (Backend for Frontend)
- Stack: Jakarta EE 10, Java 21
- Database: HSQLDB
- Build: Gradle
- Managed Entities: OrderTran, OrderDetail only
- External APIs: back-office-api, customer-hub-api
```

Reference this in every conversation.

### 4. Use Conversation History

For platforms that save history:
- Keep related work in same conversation
- Reference previous outputs: "Using the AuthResource you created earlier..."
- Start new conversation for new APIs

### 5. Verify Against Specifications

Always cross-check AI outputs with:
- `specs/baseline/system/architecture_design.md`
- Relevant API functional_design.md
- BFF pattern documentation in `resources/`

## Common Challenges and Solutions

### Challenge 1: Context Window Limits

**Solution:** Break down requests:
```
First request: Analyze architecture and list all APIs
Second request: Generate setup_tasks.md only
Third request: Generate common_tasks.md only
Fourth request: Generate API_001_auth.md only
...
```

### Challenge 2: Inconsistent Outputs

**Solution:** Be very specific:
```
Generate API_001_auth.md with EXACTLY this structure:
1. Header with: Assignee, Skills, Effort, Dependencies
2. Overview section
3. Tasks section with format:
   - [ ] **T_API001_001**: Task name
   - Purpose: ...
   - Target: ...
   - Reference SPEC: [link](path) "section"
4. Completion criteria section
```

### Challenge 3: Missing Context

**Solution:** Provide explicit context:
```
Context:
- This is a BFF (Backend for Frontend) project
- BookResource is a PROXY (no local implementation)
- OrderResource has LOCAL implementation
- Do NOT create Book, Stock, Category entities
- DO create OrderTran, OrderDetail entities

Now generate the task file for Books API.
```

### Challenge 4: Incorrect Technical Stack

**Solution:** Emphasize technical requirements:
```
CRITICAL: Use exactly these technologies:
- Jakarta EE 10 (NOT Java EE)
- JAX-RS 3.1 (NOT javax.ws.rs, use jakarta.ws.rs)
- JPA 3.1 (NOT javax.persistence, use jakarta.persistence)
- jjwt 0.12.6 for JWT
- JUnit 5 (NOT JUnit 4)

Generate AuthenResource with these constraints.
```

## Validation Checklist

After generating outputs, verify:

- [ ] File structure matches template
- [ ] All tasks have unique IDs (T_CATEGORY_NNN)
- [ ] SPEC references use Markdown links
- [ ] Technical stack matches architecture_design.md
- [ ] BFF pattern constraints are followed
- [ ] Task dependencies are correct
- [ ] Parallel markers [P] are appropriate
- [ ] Completion criteria are included

## Tips for Success

1. **Start Small:** Generate one task file, verify, then continue
2. **Be Explicit:** State constraints and requirements clearly
3. **Provide Examples:** Show desired format with examples
4. **Iterate:** Refine outputs through follow-up questions
5. **Save Progress:** Copy outputs frequently, don't lose work
6. **Test Early:** Implement and test small pieces before continuing
7. **Document Issues:** Note what works and what doesn't for your platform

## Reporting Issues

If you encounter issues with this Agent Skill on your platform:

1. Document the platform and version
2. Note the specific instruction being used
3. Describe the unexpected behavior
4. Suggest improvements for platform compatibility

This helps improve the Agent Skill for all platforms.

## Additional Resources

- **OpenAI API**: https://platform.openai.com/docs
- **Anthropic API**: https://docs.anthropic.com/
- **Google AI**: https://ai.google.dev/
- **Ollama**: https://ollama.com/
- **Agent Skills Spec**: https://github.com/agentskills/agentskills

