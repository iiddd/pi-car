# Pi-Car Security & Configuration

## 🔒 Protecting Your Credentials

This project is configured to **prevent accidentally committing sensitive information** like:
- Pi SSH credentials (username, hostname, IP addresses)
- SSH keys
- Local environment configuration

### How It Works

**Environment Variables**: All Pi connection details are stored in `.env` file which is excluded from git.

**Run Configurations**: IntelliJ run configurations automatically source `.env` before executing scripts.

### Setup

1. **Copy the example environment file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` with your Pi's credentials:**
   ```bash
   export PI_HOST=your-username@your-pi-hostname-or-ip
   export PI_PATH=/path/on/pi/to/deploy  # optional
   ```

3. **Update Remote Debug configuration** (if using):
   - Open `.run/Pi-Car Remote Debug.run.xml`
   - Replace `your-pi-hostname-or-ip` with your actual Pi hostname/IP
   - This file is gitignored in the `.idea/` directory

### Manual Usage

If running scripts manually from terminal:

```bash
# Source environment first
source .env

# Then run scripts
./scripts/deploy.sh
./scripts/status.sh
./scripts/stop.sh
```

### What's Protected

The following files are **automatically excluded** from git commits (in `.gitignore`):

- `.env` - Your local Pi credentials
- `.env.local` - Alternative local config
- `*.pem` - SSH key files
- `*.key` - Private keys
- `id_rsa*` - SSH identity files
- `*.ppk` - PuTTY private keys

### ⚠️ Before Committing

Always verify you're not committing sensitive data:

```bash
# Check what will be committed
git status
git diff

# Verify .env is not tracked
git ls-files | grep .env  # Should return nothing
```

### What's Safe to Commit

✅ **Safe to commit:**
- `.env.example` - Template with placeholder values
- Run configuration files (`.run/*.xml`) - They reference `.env` but don't contain actual credentials
- Deployment scripts - They require `PI_HOST` environment variable

❌ **NEVER commit:**
- `.env` - Your actual credentials
- SSH keys (`*.pem`, `*.key`, `id_rsa*`)
- Files with hardcoded IPs, usernames, or passwords

## 📝 Remote Debug Configuration

The Remote Debug run configuration uses a placeholder hostname. To use it:

1. Open IntelliJ IDEA
2. Go to **Run** → **Edit Configurations**
3. Select **Pi-Car Remote Debug**
4. Replace `your-pi-hostname-or-ip` with your Pi's actual hostname or IP
5. Click **OK** (this stores it locally in `.idea/` which is gitignored)

Alternatively, manually edit `.run/Pi-Car Remote Debug.run.xml` and replace the HOST value.

## 🔐 Best Practices

1. **Use SSH keys instead of passwords** for Pi authentication
2. **Never hardcode credentials** in code or scripts
3. **Review git diffs** before committing
4. **Use `.env` files** for local configuration
5. **Keep `.gitignore` updated** with sensitive file patterns

## Recovery

If you accidentally committed sensitive data:

```bash
# Remove from history (use with caution!)
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch .env" \
  --prune-empty --tag-name-filter cat -- --all

# Force push (only if you haven't shared the repo)
git push origin --force --all
```

Better yet: **Rotate your credentials** if they were exposed!

