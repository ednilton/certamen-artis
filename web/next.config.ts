import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  // Gera um servidor enxuto em .next/standalone, usado na imagem Docker
  output: 'standalone',
};

export default nextConfig;
