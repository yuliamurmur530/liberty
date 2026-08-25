const translations = {
  en: {
    skip: "Skip to content",
    navFeatures: "Features",
    navHow: "How it works",
    navPrivacy: "Privacy",
    navDownload: "Download",
    conceptLineOne: "Android creates the boundary.",
    conceptLineTwo: "Liberty makes it clear.",
    conceptStepOne: "Create the space",
    conceptStepTwo: "Install your apps",
    conceptStepThree: "Open them separately",
    heroEyebrow: "Your right to privacy.",
    heroTitle: "Important apps —<br>in a separate space.",
    heroLead: "Liberty helps separate selected apps and their data from your personal profile using Android's built-in capabilities.",
    download: "Download Liberty",
    viewCode: "View source",
    proofRoot: "No root",
    proofAds: "No ads",
    proofTracking: "No tracking",
    vpnNote: "Personal-profile tunnel is active",
    statInternet: "internet permissions",
    statTelemetry: "trackers or analytics",
    statLocal: "local setup",
    statBoundary: "creates the profile boundary",
    howEyebrow: "Simple by design",
    howTitle: "Android creates the boundary.<br><span>Liberty makes it clear.</span>",
    howLead: "A few clear steps — and selected apps live separately from your personal profile.",
    stepOneTitle: "Create the space",
    stepOneBody: "Liberty opens Android's system setup and guides you through each step.",
    stepTwoTitle: "Install your apps",
    stepTwoBody: "Use Google Play with the briefcase badge inside the protected profile.",
    stepThreeTitle: "Open them separately",
    stepThreeBody: "Apps receive separate local data and accounts.",
    featuresEyebrow: "System-level protection",
    featuresTitle: "Isolation, not camouflage.",
    featuresLead: "The operating system itself creates the boundary between personal and protected spaces.",
    featureIsolationTitle: "A separate Android profile",
    featureIsolationBody: "Selected apps are separated from personal-profile apps and data by the operating system.",
    featureRootTitle: "No root required",
    featureRootBody: "Liberty works with capabilities already built into Android.",
    featureQuietTitle: "No ads or analytics",
    featureQuietBody: "There are no advertising modules, telemetry, or remote services.",
    featureInternetTitle: "Liberty does not go online",
    featureInternetBody: "The app has no internet permission. Setup happens locally on your device.",
    galleryEyebrow: "Everything in sight",
    galleryTitle: "Nothing extra.<br>Nothing hidden.",
    galleryBody: "Clear actions, live space status and Android system badges — you always see where your apps are.",
    keyTitle: "The VPN key stays visible",
    keyBody: "In this real screenshot, the personal-profile tunnel is active while apps are kept in a separate space.",
    privacyEyebrow: "Privacy by architecture",
    privacyTitle: "Your data stays on your device.",
    privacyBody: "Liberty does not store passwords, read isolated app content, or send data outside your device.",
    privacyItemOne: "No Liberty account",
    privacyItemTwo: "No cloud dashboard",
    privacyItemThree: "No access to other apps' data",
    privacyItemFour: "Source code available for review",
    readPrivacy: "Read the privacy policy <span aria-hidden=\"true\">↗</span>",
    downloadEyebrow: "Liberty 0.4.9",
    downloadTitle: "Create your protected space.",
    downloadBody: "Your apps. Your rules. Only on your device.",
    downloadApk: "Download APK",
    releasePage: "Release page ↗",
    verifyTitle: "Verify the downloaded file",
    certLabel: "Signing certificate SHA-256",
    footerPrivacy: "Privacy",
    footerSecurity: "Security",
    footerSource: "Source code"
  }
};

const original = new Map();
document.querySelectorAll("[data-i18n]").forEach((element) => {
  original.set(element.dataset.i18n, element.innerHTML);
});

function setLanguage(language) {
  const isEnglish = language === "en";
  document.documentElement.lang = isEnglish ? "en" : "ru";
  document.documentElement.dataset.locale = isEnglish ? "en" : "ru";

  document.querySelectorAll("[data-i18n]").forEach((element) => {
    const key = element.dataset.i18n;
    element.innerHTML = isEnglish ? (translations.en[key] ?? original.get(key)) : original.get(key);
  });

  document.querySelectorAll("img[data-src-ru][data-src-en]").forEach((image) => {
    image.src = isEnglish ? image.dataset.srcEn : image.dataset.srcRu;
    image.alt = isEnglish ? image.dataset.altEn : image.dataset.altRu;
  });

  const button = document.querySelector("[data-language]");
  button.innerHTML = isEnglish
    ? '<span data-lang-label>EN</span><span class="slash">/</span><span class="muted">RU</span>'
    : '<span data-lang-label>RU</span><span class="slash">/</span><span class="muted">EN</span>';
  button.setAttribute("aria-label", isEnglish ? "Переключить на русский" : "Switch to English");
  localStorage.setItem("liberty-language", language);
}

setLanguage(localStorage.getItem("liberty-language") === "en" ? "en" : "ru");
document.querySelector("[data-language]").addEventListener("click", () => {
  setLanguage(document.documentElement.lang === "ru" ? "en" : "ru");
});

const header = document.querySelector("[data-header]");
const updateHeader = () => header.classList.toggle("scrolled", window.scrollY > 12);
updateHeader();
window.addEventListener("scroll", updateHeader, { passive: true });
