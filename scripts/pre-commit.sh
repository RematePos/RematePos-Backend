#!/bin/bash

# ============================================
# Pre-commit Hook for Microservice POS
# ============================================
# Install: cp scripts/pre-commit .git/hooks/
# Make executable: chmod +x .git/hooks/pre-commit

echo "🔍 Running pre-commit checks..."

FAILED=0

# ============================================
# Check 1: No secrets in code
# ============================================
echo ""
echo "📋 Checking for secrets..."

if git diff --cached | grep -E "(password|secret|token|api_key|AWS_ACCESS|DB_PASS)" -i; then
    echo "❌ Potential secrets found in staged changes!"
    echo "   Do not commit passwords, tokens, or secrets."
    echo "   Use .env files or secrets management instead."
    FAILED=1
else
    echo "✅ No obvious secrets detected"
fi

# ============================================
# Check 2: No debugging code
# ============================================
echo ""
echo "📋 Checking for debug code..."

if git diff --cached | grep -E "(console.log|System.out.println|debugger|pdb.set_trace)" ; then
    echo "❌ Debug code found in staged changes!"
    echo "   Remove console.log, System.out.println, etc."
    FAILED=1
else
    echo "✅ No debug code detected"
fi

# ============================================
# Check 3: Check Java syntax (if available)
# ============================================
echo ""
echo "📋 Checking Java files..."

if command -v javac &> /dev/null; then
    for file in $(git diff --cached --name-only --diff-filter=ACM | grep '.java$'); do
        if [ -f "$file" ]; then
            javac -Xlint:none "$file" 2>&1 | grep -E "error:" && FAILED=1
        fi
    done
    [ $FAILED -eq 0 ] && echo "✅ Java syntax OK"
fi

# ============================================
# Check 4: Check for large files
# ============================================
echo ""
echo "📋 Checking file sizes..."

MAX_SIZE=$((10 * 1024 * 1024))  # 10MB

for file in $(git diff --cached --name-only); do
    if [ -f "$file" ]; then
        size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null)
        if [ "$size" -gt "$MAX_SIZE" ]; then
            echo "❌ File too large: $file ($(($size / 1024 / 1024))MB)"
            echo "   Maximum size: $(($MAX_SIZE / 1024 / 1024))MB"
            echo "   Consider using Git LFS for large files"
            FAILED=1
        fi
    fi
done

[ $FAILED -eq 0 ] && echo "✅ File sizes OK"

# ============================================
# Check 5: Check XML/YAML syntax (if available)
# ============================================
echo ""
echo "📋 Checking XML/YAML files..."

if command -v xmllint &> /dev/null; then
    for file in $(git diff --cached --name-only --diff-filter=ACM | grep -E '\.(xml|pom)$'); do
        if [ -f "$file" ]; then
            xmllint "$file" > /dev/null 2>&1 || FAILED=1
        fi
    done
fi

if command -v yamllint &> /dev/null; then
    for file in $(git diff --cached --name-only --diff-filter=ACM | grep -E '\.(yml|yaml)$'); do
        if [ -f "$file" ]; then
            yamllint "$file" || FAILED=1
        fi
    done
fi

[ $FAILED -eq 0 ] && echo "✅ XML/YAML syntax OK"

# ============================================
# Check 6: Proper commit message format
# ============================================
echo ""
echo "📋 Checking commit message format..."

# Note: Message is in COMMIT_EDITMSG during commit
if [ -f "$1" ]; then
    message=$(cat "$1")

    # Should start with conventional commit type
    if echo "$message" | grep -qE "^(feat|fix|docs|style|refactor|test|chore|ci|build)(\(.+\))?:"; then
        echo "✅ Commit message format OK"
    else
        echo "⚠️  Commit message should follow conventional commits:"
        echo "    Examples:"
        echo "    - feat(customer): add new endpoint"
        echo "    - fix(customer): resolve null pointer"
        echo "    - docs: update README"
        # Not failing, just warning
    fi
fi

# ============================================
# Final Result
# ============================================
echo ""
if [ $FAILED -eq 0 ]; then
    echo "✅ All pre-commit checks passed!"
    echo ""
    exit 0
else
    echo "❌ Pre-commit checks failed!"
    echo "   Fix issues before committing."
    echo ""
    exit 1
fi

