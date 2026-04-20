# 🚀 Fixing CORS in GCP Cloud Run

## The Problem

- **Locally:** Works fine because `.env` file is in your project
- **GCP:** Fails because `.env` file is NOT in the Docker container and environment variables are NOT set in Cloud Run

---

## Solution: Set Environment Variables in Cloud Run

You need to explicitly set the `CORS_ALLOWED_ORIGINS` environment variable when deploying to Cloud Run.

---

## Step 1: Update Your Deployment

### Using gcloud CLI

```bash
gcloud run services update fineflux \
  --region asia-south1 \
  --update-env-vars CORS_ALLOWED_ORIGINS="https://fineflux.com,https://*.fineflux.com,https://finflux-64307221061.asia-south1.run.app"
```

### Complete Command with All Variables

```bash
gcloud run services update fineflux \
  --region asia-south1 \
  --update-env-vars \
    CORS_ALLOWED_ORIGINS="https://fineflux.com,https://*.fineflux.com,https://finflux-64307221061.asia-south1.run.app",\
    SPRING_DATA_MONGODB_URI="mongodb+srv://finefluxtesting:finefluxtesting123@finefluxtesting.ttyqwvc.mongodb.net/?appName=FineFluxTesting",\
    SPRING_DATA_MONGODB_DATABASE="Fos_FineFlux",\
    SPRING_MAIL_HOST="smtp.hostinger.com",\
    SPRING_MAIL_PORT="465",\
    SPRING_MAIL_USERNAME="reset@fineflux.com",\
    SPRING_MAIL_PASSWORD="Reset@!23",\
    SPRING_MAIL_FROM="reset@fineflux.com",\
    SPRING_APPLICATION_NAME="fineflux",\
    LOGGING_LEVEL_ROOT="INFO",\
    SPRING_JACKSON_TIMEZONE="Asia/Kolkata",\
    GCS_BUCKET_NAME="pulse-dev",\
    GCS_BUCKET_SERVICE_ACCOUNT_FILENAME="fineflux-db8790196c53.json"
```

---

## Step 2: Alternative - Using Cloud Console

1. Go to **Google Cloud Console** → **Cloud Run**
2. Click your service: **`fineflux`**
3. Click **EDIT & DEPLOY NEW REVISION**
4. Scroll to **Container** section
5. Click **VARIABLES & SECRETS**
6. Add/Update these environment variables:

| Name | Value |
|------|-------|
| `CORS_ALLOWED_ORIGINS` | `https://fineflux.com,https://*.fineflux.com,https://finflux-64307221061.asia-south1.run.app` |
| `SPRING_DATA_MONGODB_URI` | `mongodb+srv://finefluxtesting:finefluxtesting123@...` |
| `GCS_SERVICE_ACCOUNT_JSON` | Your complete GCS JSON |
| (Add all others from your `.env`) | |

7. Click **DEPLOY**

---

## Step 3: Verify Environment Variables Are Set

```bash
# Check the service details
gcloud run services describe fineflux --region asia-south1

# You should see all the environment variables listed
```

---

## Step 4: Check Cloud Run Logs

```bash
# View logs to verify app is reading environment variables
gcloud run services logs read fineflux --region asia-south1 --limit 50
```

Look for any errors or confirmations that environment variables are loaded.

---

## Why This Happens

### Local Environment
```
Application Start
  ↓
Load .env file ✅ (file exists in project)
  ↓
Read CORS_ALLOWED_ORIGINS ✅
  ↓
SecurityConfig uses value ✅
  ↓
CORS Works ✅
```

### GCP Cloud Run (Without Env Vars Set)
```
Application Start
  ↓
Try to load .env file ❌ (file NOT in container)
  ↓
SecurityConfig @Value tries to resolve ❌ (no env var)
  ↓
Falls back to default? (might not work properly)
  ↓
CORS Fails ❌
```

### GCP Cloud Run (With Env Vars Set)
```
Application Start
  ↓
Load environment variables ✅ (set in Cloud Run)
  ↓
Read CORS_ALLOWED_ORIGINS ✅
  ↓
SecurityConfig uses value ✅
  ↓
CORS Works ✅
```

---

## Quick Fix Command

Run this one command to update the service with CORS:

```bash
gcloud run services update fineflux --region asia-south1 --update-env-vars CORS_ALLOWED_ORIGINS="https://fineflux.com,https://*.fineflux.com,https://finflux-64307221061.asia-south1.run.app"
```

**That's it!** Your login should work after this. ✅

---

## Verify It Works

After updating, test your login from `https://fineflux.com` and it should work without CORS errors.

---

## Future Deployments

When you deploy new code, remember to set all environment variables:

```bash
# When redeploying with new image
gcloud run deploy fineflux \
  --image gcr.io/YOUR-PROJECT/fineflux:latest \
  --region asia-south1 \
  --set-env-vars CORS_ALLOWED_ORIGINS="https://fineflux.com,https://*.fineflux.com,https://finflux-64307221061.asia-south1.run.app",\
  SPRING_DATA_MONGODB_URI="mongodb+srv://..."
  # ... other vars
```

---

## Summary

| Environment | .env File | Env Vars Set | Works |
|-------------|-----------|--------------|-------|
| Local Dev | ✅ YES | Not needed | ✅ YES |
| GCP (before fix) | ❌ NO | ❌ NO | ❌ NO |
| GCP (after fix) | ❌ NO | ✅ YES | ✅ YES |

---

## Next Action

Run this command now:

```bash
gcloud run services update fineflux --region asia-south1 --update-env-vars CORS_ALLOWED_ORIGINS="https://fineflux.com,https://*.fineflux.com,https://finflux-64307221061.asia-south1.run.app"
```

Then test your login. It should work! 🎉

