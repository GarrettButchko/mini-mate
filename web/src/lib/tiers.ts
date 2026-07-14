import type { ElementType } from "react";
import {
  MapPin, Sliders, Link2, Palette, Megaphone, Image, Trophy,
  Zap, Gift, Nfc, TrendingUp, RefreshCw, Star, Mail, Download,
  BarChart2, Building2, Layers,
} from "lucide-react";

export type TierFeature = { icon: ElementType; text: string; soon?: boolean };

export type Tier = {
  name: string;
  price: string;
  period: string;
  description: string;
  tierIcon: ElementType;
  inherits?: string;
  features: TierFeature[];
  cta: string;
  href: string;
  highlight: boolean;
};

export const tiers: Tier[] = [
  {
    name: "Listed",
    price: "Free",
    period: "forever",
    description: "Get discovered. Put your course on the Mini Mate map.",
    tierIcon: MapPin,
    features: [
      { icon: MapPin, text: "Course listed on the Mini Mate map" },
      { icon: Sliders, text: "Custom par info for every hole" },
      { icon: Link2, text: "Social links on your course profile" },
    ],
    cta: "Get Listed",
    href: "/find-course",
    highlight: false,
  },
  {
    name: "Partner",
    price: "$29",
    period: "/ month",
    description: "Full brand presence across the Mini Mate app and real-world marketing.",
    tierIcon: Building2,
    inherits: "Everything in Listed, plus:",
    features: [
      { icon: Palette, text: "Custom scorecard colors & accent" },
      { icon: Megaphone, text: "Your ad in scorecards & app pages" },
      { icon: Image, text: "Your logo in the app home & scorecard" },
      { icon: Trophy, text: "Managed course leaderboard" },
      { icon: Zap, text: "Tournament mode access", soon: true },
      { icon: Gift, text: "Mini Mate pitch tent & marketing kit" },
      { icon: Nfc, text: "NFC-enabled tent tag (Mini Mate branded)" },
    ],
    cta: "Choose Partner",
    href: "/find-course",
    highlight: true,
  },
  {
    name: "Analytics",
    price: "$69",
    period: "/ month",
    description: "Everything in Partner plus deep player insights and data exports.",
    tierIcon: BarChart2,
    inherits: "Everything in Partner, plus:",
    features: [
      { icon: TrendingUp, text: "Growth analytics dashboard" },
      { icon: RefreshCw, text: "Player retention tracking" },
      { icon: Star, text: "Experience score metrics" },
      { icon: Mail, text: "Player email list from your course" },
      { icon: Download, text: "CSV export — all data, any time" },
      { icon: Layers, text: "Beautiful analytics interface" },
    ],
    cta: "Choose Analytics",
    href: "/find-course",
    highlight: false,
  },
];
