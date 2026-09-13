# Contributing to IDA Pro Mobile

Thank you for your interest in contributing to IDA Pro Mobile! This document provides guidelines and information for contributors.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)

## Code of Conduct

This project adheres to a Code of Conduct that we expect project participants to abide by:

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android NDK 25.1.8937393
- JDK 17
- Git for version control
- Basic knowledge of Kotlin, Jetpack Compose, and C++

### Development Environment Setup
1. **Fork and Clone**
   ```bash
   git clone https://github.com/your-username/ida-pro-mobile.git
   cd ida-pro-mobile
   ```

2. **Install Dependencies**
   - Open project in Android Studio
   - Install NDK through SDK Manager
   - Sync project to download Gradle dependencies

3. **Verify Setup**
   ```bash
   ./gradlew test
   ./gradlew lint
   ```

## Development Workflow

### Branch Strategy
- `main`: Production-ready code
- `develop`: Development integration branch
- `feature/*`: New features
- `bugfix/*`: Bug fixes
- `hotfix/*`: Critical production fixes

### Workflow Steps
1. Create feature branch from `develop`
2. Implement changes with tests
3. Run local verification
4. Submit pull request
5. Address review feedback
6. Merge after approval

## Coding Standards

### Kotlin Guidelines
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use ktlint for automatic formatting
- Prefer immutable data structures
- Use descriptive variable and function names
- Document public APIs with KDoc

### C++ Guidelines
- Follow [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html)
- Use consistent indentation (4 spaces)
- Prefer RAII for resource management
- Use const-correctness
- Document complex algorithms

### UI/UX Guidelines
- Follow Material Design 3 principles
- Ensure accessibility compliance
- Support dark/light themes
- Test on various screen sizes
- Maintain consistent navigation patterns

## Testing Guidelines

### Unit Tests
- Write tests for all business logic
- Mock external dependencies
- Use descriptive test names
- Aim for high code coverage
- Test edge cases and error conditions

### Integration Tests
- Test database operations
- Verify JNI bridge functionality
- Test file I/O operations
- Validate UI component interactions

### Testing Commands
```bash
# Run all tests
./gradlew test

# Run specific test suite
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedAndroidTest

# Run with coverage
./gradlew testDebugUnitTestCoverage
```

## Pull Request Process

### Before Submitting
1. **Update Documentation**: Ensure README and code documentation are current
2. **Run Tests**: All tests must pass
3. **Code Quality**: Pass lint and detekt checks
4. **Self Review**: Review your own changes thoroughly

### PR Requirements
- **Clear Title**: Descriptive and concise
- **Description**: What, why, and how of the changes
- **Screenshots**: For UI changes
- **Testing**: Description of testing performed
- **Breaking Changes**: Clearly marked if any

### PR Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Screenshots (if applicable)
<!-- Add screenshots for UI changes -->

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Tests added/updated
```

### Review Process
1. **Automated Checks**: CI pipeline must pass
2. **Code Review**: At least one maintainer approval
3. **Testing**: Reviewer verification of functionality
4. **Documentation**: Ensure completeness and accuracy

## Issue Reporting

### Bug Reports
Use the bug report template with:
- **Environment**: Device, OS version, app version
- **Steps to Reproduce**: Clear, numbered steps
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Screenshots/Logs**: If applicable

### Feature Requests
Use the feature request template with:
- **Problem Statement**: What problem does this solve?
- **Proposed Solution**: How should it work?
- **Alternatives**: Other solutions considered
- **Additional Context**: Supporting information

### Security Issues
Report security vulnerabilities privately to maintainers before public disclosure.

## Architecture Contributions

### Adding New Features
1. **Design Discussion**: Open issue for design discussion
2. **Architecture Review**: Ensure alignment with existing patterns
3. **Implementation Plan**: Break down into manageable pieces
4. **Documentation**: Update architecture docs

### Native Code Changes
- **Performance Testing**: Benchmark critical paths
- **Memory Safety**: Use memory-safe patterns
- **Cross-Platform**: Consider portability
- **Documentation**: Document complex algorithms

## Release Process

### Version Numbering
Follow [Semantic Versioning](https://semver.org/):
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Checklist
- [ ] Version number updated
- [ ] Changelog updated
- [ ] Tests passing
- [ ] Documentation current
- [ ] Security review completed

## Getting Help

### Communication Channels
- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and ideas
- **Pull Request Comments**: Code-specific discussions

### Resources
- [Android Developer Docs](https://developer.android.com/)
- [Jetpack Compose Guide](https://developer.android.com/jetpack/compose)
- [Android NDK Guide](https://developer.android.com/ndk)
- [Material Design 3](https://m3.material.io/)

## Recognition

Contributors will be recognized in:
- Release notes for significant contributions
- README contributors section
- Special mentions for outstanding contributions

Thank you for contributing to IDA Pro Mobile!