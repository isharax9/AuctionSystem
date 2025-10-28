# Contributing to Distributed Online Auction System

First off, thank you for considering contributing to the Auction System! 🎉

It's people like you that make this project such a great learning and development tool. This document provides guidelines for contributing to the project.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Pull Requests](#pull-requests)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Review Process](#review-process)

---

## 📜 Code of Conduct

### Our Pledge

In the interest of fostering an open and welcoming environment, we as contributors and maintainers pledge to make participation in our project and our community a harassment-free experience for everyone, regardless of age, body size, disability, ethnicity, sex characteristics, gender identity and expression, level of experience, education, socio-economic status, nationality, personal appearance, race, religion, or sexual identity and orientation.

### Our Standards

**Examples of behavior that contributes to creating a positive environment include:**

- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Gracefully accepting constructive criticism
- Focusing on what is best for the community
- Showing empathy towards other community members

**Examples of unacceptable behavior include:**

- The use of sexualized language or imagery and unwelcome sexual attention or advances
- Trolling, insulting/derogatory comments, and personal or political attacks
- Public or private harassment
- Publishing others' private information without explicit permission
- Other conduct which could reasonably be considered inappropriate in a professional setting

### Enforcement

Instances of abusive, harassing, or otherwise unacceptable behavior may be reported by contacting the project team at [isharax9@gmail.com](mailto:isharax9@gmail.com). All complaints will be reviewed and investigated promptly and fairly.

---

## 🤝 How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When you create a bug report, include as many details as possible.

**Bug Report Template:**

```markdown
**Description**
A clear and concise description of the bug.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '...'
3. Scroll down to '...'
4. See error

**Expected Behavior**
What you expected to happen.

**Actual Behavior**
What actually happened.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Environment:**
- OS: [e.g., Windows 10, macOS, Linux]
- Java Version: [e.g., 11, 17]
- GlassFish Version: [e.g., 7.0.0]
- Browser: [e.g., Chrome 120, Firefox 121]

**Additional Context**
Add any other context about the problem here.

**Logs**
```
Paste relevant log output here
```
```

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, include:

**Enhancement Template:**

```markdown
**Is your feature request related to a problem?**
A clear and concise description of what the problem is.

**Describe the solution you'd like**
A clear and concise description of what you want to happen.

**Describe alternatives you've considered**
A clear and concise description of any alternative solutions or features.

**Additional context**
Add any other context or screenshots about the feature request.

**Would you like to implement this feature?**
Yes/No - If yes, indicate your timeline.
```

### Pull Requests

Pull requests are the best way to propose changes to the codebase. We actively welcome your pull requests:

1. **Fork the repo** and create your branch from `main`
2. **Make your changes** following our coding standards
3. **Add tests** if you've added code that should be tested
4. **Update documentation** if you've changed APIs or functionality
5. **Ensure the test suite passes** (`mvn test`)
6. **Make sure your code lints** (`mvn checkstyle:check`)
7. **Issue the pull request!**

**Pull Request Template:**

```markdown
## Description
Brief description of the changes.

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update
- [ ] Code refactoring
- [ ] Performance improvement

## Related Issue
Fixes #(issue number)

## Changes Made
- Change 1
- Change 2
- Change 3

## Testing
Describe the tests you ran and how to reproduce them.

## Screenshots (if applicable)
Add screenshots to demonstrate the changes.

## Checklist
- [ ] My code follows the project's style guidelines
- [ ] I have performed a self-review of my code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing unit tests pass locally with my changes
- [ ] Any dependent changes have been merged and published
```

---

## 🛠️ Development Setup

### Prerequisites

- JDK 11 or higher
- Maven 3.6+
- GlassFish 7.x
- Git
- Your favorite IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Setup Steps

1. **Fork and Clone**
   ```bash
   git clone https://github.com/YOUR_USERNAME/AuctionSystem.git
   cd AuctionSystem
   ```

2. **Create a Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Build the Project**
   ```bash
   mvn clean install
   ```

4. **Run Tests**
   ```bash
   mvn test
   ```

5. **Start GlassFish**
   ```bash
   cd $GLASSFISH_HOME/bin
   ./asadmin start-domain domain1
   ```

6. **Deploy**
   ```bash
   mvn package
   ./asadmin deploy target/AuctionSystem.war
   ```

### IDE Configuration

**IntelliJ IDEA:**
1. Import as Maven project
2. Set JDK to 11+
3. Enable annotation processing
4. Configure GlassFish server in Run/Debug configurations

**Eclipse:**
1. Import → Existing Maven Projects
2. Right-click project → Properties → Java Build Path → Set JDK 11+
3. Install GlassFish Tools plugin
4. Configure server runtime

---

## 📏 Coding Standards

### Java Code Style

We follow standard Java conventions with some specific guidelines:

**Formatting:**
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use braces for all control structures
- One statement per line

**Naming Conventions:**
- Classes: `PascalCase` (e.g., `AuctionServiceBean`)
- Methods: `camelCase` (e.g., `createAuction()`)
- Variables: `camelCase` (e.g., `auctionId`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_BID_AMOUNT`)
- Packages: lowercase (e.g., `com.auction.ejb`)

**Best Practices:**

```java
// Good - Descriptive names, proper formatting
public AuctionDTO createAuction(String title, String description, 
                               double startingPrice, int hours) {
    if (title == null || title.trim().isEmpty()) {
        throw new IllegalArgumentException("Title cannot be empty");
    }
    
    Long auctionId = generateAuctionId();
    Auction auction = new Auction(auctionId, title, description, startingPrice);
    
    return convertToDTO(auction);
}

// Bad - Poor naming, no validation
public AuctionDTO ca(String t, String d, double p, int h) {
    Long id = cnt++;
    Auction a = new Auction(id, t, d, p);
    return toDTO(a);
}
```

**Documentation:**

```java
/**
 * Creates a new auction with the specified parameters.
 * 
 * @param title the auction title, must not be null or empty
 * @param description detailed description of the item
 * @param startingPrice the minimum starting bid amount
 * @param hours auction duration in hours
 * @return the created auction DTO
 * @throws IllegalArgumentException if title is empty or price is negative
 */
public AuctionDTO createAuction(String title, String description, 
                               double startingPrice, int hours) {
    // Implementation
}
```

### EJB Specific Guidelines

1. **Always use Remote interfaces** for distributed access
2. **Mark transaction boundaries** explicitly with `@TransactionAttribute`
3. **Handle concurrency** with appropriate locking mechanisms
4. **Log important operations** using java.util.logging
5. **Validate all inputs** before processing

### Exception Handling

```java
// Good - Specific exception handling with logging
try {
    Auction auction = auctionService.getAuction(auctionId);
    if (auction == null) {
        throw new AuctionNotFoundException("Auction not found: " + auctionId);
    }
    return auction;
} catch (AuctionNotFoundException e) {
    logger.warning("Auction not found: " + e.getMessage());
    throw e;
} catch (Exception e) {
    logger.severe("Unexpected error: " + e.getMessage());
    throw new ServiceException("Failed to retrieve auction", e);
}

// Bad - Generic catch-all without context
try {
    return auctionService.getAuction(auctionId);
} catch (Exception e) {
    e.printStackTrace();
    return null;
}
```

---

## 🧪 Testing Guidelines

### Test Requirements

- All new features must include unit tests
- Maintain test coverage above 80%
- Integration tests for complex workflows
- Performance tests for critical operations

### Test Structure

```java
@Test
@DisplayName("Should create auction with valid parameters")
void testCreateAuction_ValidParameters() {
    // Arrange
    String title = "Test Auction";
    String description = "Test Description";
    double startingPrice = 100.0;
    int hours = 2;
    
    // Act
    AuctionDTO result = auctionService.createAuction(
        title, description, startingPrice, hours
    );
    
    // Assert
    assertNotNull(result);
    assertEquals(title, result.getTitle());
    assertEquals(startingPrice, result.getStartingPrice());
    assertTrue(result.isActive());
}
```

### Test Categories

1. **Unit Tests**: Test individual methods and components
2. **Integration Tests**: Test component interactions
3. **Concurrency Tests**: Test thread-safety and race conditions
4. **Performance Tests**: Validate performance requirements

### Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=AuctionServiceBeanTest

# With coverage
mvn test jacoco:report

# Skip tests (for quick builds only)
mvn package -DskipTests
```

---

## 💬 Commit Message Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, semicolons, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Maintenance tasks
- `ci`: CI/CD changes

### Examples

```bash
# Feature
feat(auction): add auction duration validation

Add validation for auction duration to ensure it's between 1 minute and 7 days.

Closes #123

# Bug fix
fix(bid): prevent negative bid amounts

Added validation to reject bids with negative amounts. This prevents
data corruption and ensures auction integrity.

Fixes #456

# Documentation
docs(readme): update installation instructions

Updated GlassFish installation steps and added Docker deployment option.

# Breaking change
feat(api)!: change auction creation endpoint

BREAKING CHANGE: The auction creation endpoint now requires duration
in hours and minutes instead of just hours.

Migration: Update your API calls to include both durationHours and 
durationMinutes parameters.
```

---

## 🔍 Review Process

### Pull Request Review

All pull requests go through the following review process:

1. **Automated Checks**
   - Build must pass (`mvn clean install`)
   - All tests must pass (`mvn test`)
   - Code coverage must meet threshold (80%+)
   - No critical security vulnerabilities

2. **Code Review**
   - At least one approving review required
   - Changes requested must be addressed
   - All conversations must be resolved

3. **Documentation Review**
   - README updated if needed
   - API documentation current
   - Code comments adequate

4. **Testing Review**
   - Appropriate test coverage
   - Edge cases considered
   - Performance implications evaluated

### Review Checklist

**For Reviewers:**

- [ ] Code follows project style guidelines
- [ ] Changes are well-documented
- [ ] Tests cover new functionality
- [ ] No unnecessary complexity
- [ ] Security implications considered
- [ ] Performance implications considered
- [ ] Breaking changes properly documented
- [ ] Backwards compatibility maintained (if applicable)

**For Contributors:**

- [ ] Self-reviewed the code
- [ ] Added/updated tests
- [ ] Updated documentation
- [ ] No linting errors
- [ ] All tests passing
- [ ] Commits follow convention
- [ ] PR description is clear

---

## 📞 Getting Help

If you need help with your contribution:

1. **Check Documentation**: README and this guide
2. **Search Issues**: Someone might have had the same question
3. **Ask in Discussions**: Use GitHub Discussions for questions
4. **Contact Maintainers**: 
   - Email: [isharax9@gmail.com](mailto:isharax9@gmail.com)
   - Telegram: [@mac_knight141](https://t.me/mac_knight141)

---

## 🎉 Recognition

Contributors will be recognized in the following ways:

- Listed in the README acknowledgments
- GitHub contributor badge
- Special mention in release notes for significant contributions

---

## 📝 License

By contributing to this project, you agree that your contributions will be licensed under the MIT License.

---

## 🙏 Thank You!

Your contributions help make this project better for everyone. We appreciate your time and effort in improving the Distributed Online Auction System!

**Happy Coding! 🚀**
