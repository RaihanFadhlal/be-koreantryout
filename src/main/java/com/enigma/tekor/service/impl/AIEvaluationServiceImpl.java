package com.enigma.tekor.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.enigma.tekor.dto.request.AIEvaluationRequest;
import com.enigma.tekor.dto.request.UserAnswerEvaluationRequest;
import com.enigma.tekor.service.AIEvaluationService;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AIEvaluationServiceImpl implements AIEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(AIEvaluationServiceImpl.class);

    @Value("${google.api.key}")
    private String googleApiKey;
    
    private Client client;
    private static final String MODEL_NAME = "gemini-2.0-flash-001";
    private static final String GENERIC_ERROR_MESSAGE = "Maaf, terjadi kesalahan saat mencoba mendapatkan evaluasi dari AI.";

    @PostConstruct
    public void init() {
        try {
            this.client = Client.builder().apiKey(googleApiKey).build();
        
            log.info("Unified Gemini Client initialized successfully.");
            if (client.vertexAI()) {
                log.info("Client is configured to use Vertex AI backend.");
            } else {
                log.info("Client is configured to use Gemini Developer API backend.");
            }
        } catch (Exception e) {
            log.error("Fatal: Error initializing Unified Gemini Client. The service will not work.", e);
            throw new RuntimeException("Could not initialize Unified Gemini Client", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("AIEvaluationService is shutting down.");
    }

    @Override
    public Mono<String> getEvaluation(AIEvaluationRequest request) {
        String promptText = buildPrompt(request);
        log.info("Sending evaluation prompt for TestAttempt ID: {}", request.getTestAttemptId());

        return Mono.fromCallable(() -> {
            log.debug("Executing blocking call to Gemini API on a dedicated thread.");
            GenerateContentResponse response = client.models.generateContent(MODEL_NAME, promptText, null);
            return response.text();
        })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> log.error("Error in reactive chain for Gemini call for TestAttempt ID: {}",
                        request.getTestAttemptId(), error))
                .onErrorReturn(GENERIC_ERROR_MESSAGE);
    }

    private String buildPrompt(AIEvaluationRequest request) {
        Map<String, List<UserAnswerEvaluationRequest>> answersByQuestionType = request.getUserAnswers().stream()
                .collect(Collectors.groupingBy(UserAnswerEvaluationRequest::getQuestionType));

        StringBuilder details = new StringBuilder();
        answersByQuestionType.forEach((questionType, answers) -> {
            long correctCount = answers.stream().filter(UserAnswerEvaluationRequest::getIsCorrect).count();
            long incorrectCount = answers.size() - correctCount;
            details.append(String.format("""

                    Jenis Soal: %s
                    - Jawaban Benar: %d
                    - Jawaban Salah: %d
                    """, questionType, correctCount, incorrectCount));
        });

        return """
                Anda adalah seorang evaluator ahli untuk ujian EPS-TOPIK (Employment Permit System - Test of Proficiency in Korean) dengan pengalaman 
                lebih dari 10 tahun dalam mengajar bahasa Korea dan mempersiapkan siswa untuk ujian ini.

                KONTEKS UJIAN:
                - Seorang peserta ujian baru saja menyelesaikan simulasi ujian EPS-TOPIK
                - Hasil ujian: Skor Total %.0f dari 100 poin
                - Detail per kategori: %s

                TUGAS ANDA:
                Berikan evaluasi komprehensif dan umpan balik yang terpersonalisasi dalam format markdown yang mencakup:

                ### 1. ANALISIS HASIL
                - **Interpretasi skor**: Jelaskan posisi skor dalam skala EPS-TOPIK (0-100)
                - **Kategori kemampuan**: Tentukan level kemampuan saat ini (Pemula/Menengah/Mahir)
                - **Perbandingan standar**: Bandingkan dengan skor minimum kelulusan (90 poin)

                ### 2. IDENTIFIKASI KEKUATAN & KELEMAHAN
                - **Area yang dikuasai**: Highlight kategori dengan performa terbaik
                - **Area yang perlu diperbaiki**: Identifikasi kelemahan utama dengan spesifik
                - **Pola kesalahan**: Analisis jenis kesalahan yang sering muncul

                ### 3. STRATEGI PERBAIKAN TERSTRUKTUR
                Untuk setiap kelemahan yang diidentifikasi, berikan:
                - **Diagnosis masalah**: Penjelasan mengapa area ini sulit
                - **Langkah konkret**: 3-5 action items yang dapat dilakukan
                - **Sumber belajar**: Rekomendasi materi atau metode belajar
                - **Timeline**: Perkiraan waktu yang diperlukan untuk perbaikan

                ### 4. RENCANA BELAJAR JANGKA PENDEK (1-2 bulan)
                - **Target mingguan**: Breakdown tujuan per minggu
                - **Jadwal harian**: Alokasi waktu belajar yang realistis
                - **Milestone**: Indikator progres yang terukur

                ### 5. MOTIVASI & GROWTH MINDSET
                - **Pengakuan progres**: Apresiasi pencapaian yang sudah ada
                - **Reframing tantangan**: Ubah perspektif kesulitan menjadi peluang
                - **Inspirasi**: Kisah sukses peserta EPS-TOPIK lainnya
                - **Affirmasi positif**: Kalimat penyemangat yang personal

                ### 6. TIPS PRAKTIS UJIAN
                - **Manajemen waktu**: Strategi pengerjaan soal yang efektif
                - **Teknik eliminasi**: Cara mengurangi pilihan jawaban
                - **Persiapan mental**: Tips mengatasi kecemasan ujian

                GAYA PENULISAN:
                - Gunakan bahasa Indonesia yang hangat, empatik, dan memotivasi
                - Hindari jargon teknis yang rumit
                - Sertakan emoji yang relevan untuk membuat lebih engaging
                - Gunakan format yang mudah dibaca dengan headers, bullet points, dan highlight
                - Berikan contoh konkret dan aplikatif
                - Tutup dengan kata-kata penyemangat yang kuat

                TONE: Seperti mentor yang berpengalaman, supportif namun jujur, dan percaya pada potensi siswa untuk berkembang.
                                """
                .formatted(request.getScore(), details.toString());
    }
}