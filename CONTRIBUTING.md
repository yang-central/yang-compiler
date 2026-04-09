# Contributing to YANG Compiler

Thank you for your interest in contributing to YANG Compiler! This document provides guidelines and instructions for contributing.

## 🚀 Getting Started

### Prerequisites
- JDK 8 or higher
- Maven 3.6+
- Git

### Setup Development Environment

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/yang-compiler.git
   cd yang-compiler
   ```
3. Add upstream remote:
   ```bash
   git remote add upstream https://github.com/yang-central/yang-compiler.git
   ```
4. Build the project:
   ```bash
   mvn clean install
   ```

## 📝 How to Contribute

### Reporting Bugs

Before creating bug reports, please check existing issues. When creating a bug report, include:

- **Clear title and description**
- **Steps to reproduce** the behavior
- **Expected vs actual behavior**
- **Environment details** (OS, Java version, Maven version)
- **Example YANG files** if applicable
- **Error messages** or stack traces

**Example:**
```markdown
**Describe the bug**
YangCompiler fails to resolve circular dependencies between modules A and B.

**To Reproduce**
1. Create module A that imports module B
2. Create module B that imports module A
3. Run: yangc option=build.json

**Expected behavior**
Should detect circular dependency and provide clear error message.

**Actual behavior**
StackOverflowError occurs.
```

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. Include:

- **Use case**: Why is this feature needed?
- **Proposed solution**: How should it work?
- **Alternatives considered**: Other approaches you've thought about
- **Additional context**: Examples, mockups, or references

### Pull Requests

1. **Create a branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following our coding standards

3. **Add tests** for new functionality

4. **Ensure all tests pass**:
   ```bash
   mvn clean test
   ```

5. **Commit your changes** with clear messages:
   ```bash
   git commit -m "feat: add support for YANG Diff plugin"
   ```

6. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Open a Pull Request** against the `main` branch

## 💻 Coding Standards

### Java Code Style

- Follow [Oracle's Java Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-introduction.html)
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use meaningful variable and method names
- Add Javadoc comments for public APIs

### Commit Message Format

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples:**
```
feat(plugin): add YANG documentation generator

fix(parser): handle circular module dependencies correctly

docs(readme): update installation instructions for Windows
```

### Testing Guidelines

- Write unit tests for new features
- Maintain test coverage above 70%
- Name test methods descriptively: `shouldReturnEmptyListWhenNoModulesFound()`
- Use meaningful test data
- Test both success and failure scenarios

## 🔧 Development Workflow

### Building the Project

```bash
# Clean build
mvn clean install

# Skip tests for faster builds
mvn clean install -DskipTests

# Build with specific profile
mvn clean package -P release
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=YangCompilerTest

# Run tests with debug output
mvn test -X
```

### Using Docker for Development

```bash
# Build Docker image
docker build -t yang-compiler:dev .

# Run container
docker run -v $(pwd)/yang:/opt/yang-compiler/yang yang-compiler:dev
```

## 📚 Plugin Development

### Creating a New Plugin

1. Implement `YangCompilerPlugin` interface:
   ```java
   public class MyPlugin implements YangCompilerPlugin {
       @Override
       public String getName() {
           return "my_plugin";
       }
       
       @Override
       public void execute(YangCompilerContext context) {
           // Plugin logic here
       }
   }
   ```

2. Register in `plugins.json`:
   ```json
   {
     "name": "my_plugin",
     "class": "com.example.MyPlugin",
     "description": "My custom plugin"
   }
   ```

3. Add tests for your plugin

### Plugin Best Practices

- Keep plugins focused on a single responsibility
- Provide clear error messages
- Support configuration via parameters
- Document usage examples
- Handle edge cases gracefully

## 📖 Documentation

### Updating Documentation

- Update `README.md` for user-facing changes
- Update `docs/` directory for detailed guides
- Add inline code comments for complex logic
- Include examples where helpful

### Writing Good Documentation

- Use clear, concise language
- Provide practical examples
- Include troubleshooting tips
- Link to related resources

## 🎯 Release Process

Releases are managed by maintainers. The process includes:

1. Version bump in `pom.xml`
2. Run full test suite
3. Create release tag
4. Build and sign artifacts
5. Deploy to Maven Central
6. Create GitHub release with changelog

## 💬 Community

- **Questions?** Open an issue with the `question` label
- **Discussions:** Use GitHub Discussions for general topics
- **Code of Conduct:** Be respectful and inclusive

## 🙏 Recognition

Contributors are recognized in:
- GitHub Contributors page
- Release notes
- Project documentation

Thank you for contributing to YANG Compiler! 🎉
