package com.application.smartstation.ui.activity

import android.net.Uri
import android.os.Bundle
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityPdfViewBinding
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.viewBinding
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.shockwave.pdfium.PdfDocument
import java.io.File

class PdfViewActivity : BaseActivity(), OnPageChangeListener, OnLoadCompleteListener,
    OnPageErrorListener {

    val binding: ActivityPdfViewBinding by viewBinding()
    var path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun initView() {
        if (intent != null) {
            path = intent.getStringExtra("path")!!
        }
        val fileName = FileUtils.getFileNameFromPath(path).replace("/", "")
        binding.ilHeader.txtHeader.text = fileName

        binding.pdfView.fromUri(Uri.fromFile(File(path)))
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .onLoad(this)
            .scrollHandle(DefaultScrollHandle(this))
            .spacing(10) // in dp
            .onPageError(this)
            .load()
    }

    override fun onPageChanged(page: Int, pageCount: Int) {

    }

    override fun loadComplete(nbPages: Int) {
        val meta: PdfDocument.Meta = binding.pdfView.documentMeta
    }

    override fun onPageError(page: Int, t: Throwable?) {
    }
}