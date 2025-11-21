# Documentation Guide

This file explains the RentKar documentation structure and how to navigate it.

## 📚 Documentation Files

### Core Documents (Start Here)
1. **README.md** - Main entry point with project overview, quick start, and features
2. **DOCS_SUMMARY.md** - Quick reference to all documentation
3. **DOCS_INDEX.md** - Detailed navigation guide with links to all docs

### Project Planning
- **PRD.md** - Product Requirements Document
- **PROJECT_STATUS.md** - Current progress and milestones
- **CHANGELOG.md** - Detailed change history
- **TEAM_ROLES.md** - Team structure and responsibilities

### Technical Documentation
- **ARCHITECTURE.md** - System design and tech stack
- **API_CONTRACT.md** - Complete API specifications
- **AUTHENTICATION.md** - Authentication system details
- **SETUP_GUIDE.md** - Installation and configuration

### Feature Guides
- **AI_GENERATION_GUIDE.md** - Complete guide to AI-powered features

### Design
- **WIREFRAMES.md** - UI/UX designs and mockups

### Spec-Driven Development
Located in `.kiro/specs/[feature-name]/`:
- `requirements.md` - User stories and acceptance criteria
- `design.md` - Architecture and correctness properties
- `tasks.md` - Implementation plan

## 🗂️ Documentation Organization

```
RentKar/
├── README.md                    # Main entry point
├── DOCS_SUMMARY.md             # Quick reference
├── DOCS_INDEX.md               # Navigation guide
├── PROJECT_STATUS.md           # Current progress
├── CHANGELOG.md                # Change history
├── PRD.md                      # Requirements
├── ARCHITECTURE.md             # System design
├── API_CONTRACT.md             # API specs
├── AUTHENTICATION.md           # Auth details
├── AI_GENERATION_GUIDE.md      # AI feature guide
├── SETUP_GUIDE.md              # Setup instructions
├── WIREFRAMES.md               # UI designs
├── TEAM_ROLES.md               # Team structure
└── .kiro/specs/                # Feature specs
    ├── user-authentication/
    ├── item-management/
    └── ai-description-generation/
```

## 🎯 Finding What You Need

### "I want to run the app"
→ Start with **README.md** Quick Start section

### "I need to set up from scratch"
→ Follow **SETUP_GUIDE.md**

### "I want to understand the system"
→ Read **ARCHITECTURE.md**

### "I need API details"
→ Check **API_CONTRACT.md**

### "I want to use AI features"
→ See **AI_GENERATION_GUIDE.md**

### "I want to know what's done"
→ Check **PROJECT_STATUS.md** and **CHANGELOG.md**

### "I want to see all docs"
→ Browse **DOCS_INDEX.md** or **DOCS_SUMMARY.md**

## 📝 Documentation Standards

### File Naming
- Use UPPERCASE for main docs (README.md, ARCHITECTURE.md)
- Use descriptive names (AI_GENERATION_GUIDE.md, not AI.md)
- Use underscores for multi-word files

### Content Structure
- Start with a clear title and brief description
- Use markdown headers for organization
- Include table of contents for long documents
- Add "Last Updated" date at the bottom

### Cross-References
- Link to related documents
- Use relative paths (./FILE.md)
- Keep links up to date

## 🔄 Keeping Docs Updated

### When to Update
- **README.md**: When adding major features or changing setup
- **PROJECT_STATUS.md**: At the end of each week
- **CHANGELOG.md**: When completing features or milestones
- **API_CONTRACT.md**: When adding/changing endpoints
- **DOCS_INDEX.md**: When adding new documentation files

### Update Checklist
- [ ] Update relevant documentation files
- [ ] Update PROJECT_STATUS.md progress
- [ ] Add entry to CHANGELOG.md
- [ ] Update DOCS_INDEX.md if new files added
- [ ] Update cross-references in related docs
- [ ] Update "Last Updated" dates

## 🚫 Removed Files

The following files were removed during consolidation:
- **QUICK_START.md** - Content merged into README.md (redundant)

## 📊 Documentation Metrics

- **Total Documentation Files**: 14 (excluding specs)
- **Spec Directories**: 3 (authentication, item-management, ai-description-generation)
- **Total Lines of Documentation**: ~5,000+
- **Last Major Update**: Week 5 (November 21, 2025)

---

**Maintained by**: RentKar Development Team  
**Last Updated**: November 21, 2025

